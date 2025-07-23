/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoomoney.sdk.kassa.payments.paymentAuth.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.yoomoney.sdk.kassa.payments.api.PaymentsAuthApi
import ru.yoomoney.sdk.kassa.payments.api.SuspendResultCallAdapterFactory
import ru.yoomoney.sdk.kassa.payments.api.YooKassaJacksonConverterFactory
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.AuthPaymentsHttpClient
import ru.yoomoney.sdk.kassa.payments.di.scope.CheckoutScope
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.ApiV3PaymentAuthRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.MockPaymentAuthTypeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.MockProcessPaymentAuthRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.MockSmsSessionRetryRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthAnalytics
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthBusinessLogic
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTypeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthUseCase
import ru.yoomoney.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentAuth.RequestPaymentAuthUseCase
import ru.yoomoney.sdk.kassa.payments.paymentAuth.RequestPaymentAuthUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentAuth.SelectAppropriateAuthType
import ru.yoomoney.sdk.kassa.payments.paymentAuth.SmsSessionRetryRepository
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.yooprofiler.YooProfiler

@Module
internal class PaymentAuthModule {

    @Provides
    @CheckoutScope
    fun paymentsAuthApi(
        hostProvider: HostProvider,
        @AuthPaymentsHttpClient okHttpClient: OkHttpClient,
        apiErrorMapper: ApiErrorMapper
    ): PaymentsAuthApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(hostProvider.paymentAuthorizationHost()+ "/" )
            .addConverterFactory(YooKassaJacksonConverterFactory.create(jacksonBaseObjectMapper))
            .addCallAdapterFactory(SuspendResultCallAdapterFactory(apiErrorMapper))
            .build()
            .create(PaymentsAuthApi::class.java)
    }

    @Provides
    @CheckoutScope
    fun apiV3PaymentAuthRepository(
        profiler: YooProfiler,
        profilingSessionIdStorage: ProfilingSessionIdStorage,
        paymentsAuthApi: PaymentsAuthApi
    ): ApiV3PaymentAuthRepository {
        return ApiV3PaymentAuthRepository(
            profilingSessionIdStorage = profilingSessionIdStorage,
            profiler = profiler,
            selectAppropriateAuthType = SelectAppropriateAuthType(),
            paymentsAuthApi = paymentsAuthApi
        )
    }

    @Provides
    @CheckoutScope
    fun paymentAuthTypeRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): PaymentAuthTypeRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockPaymentAuthTypeRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    @CheckoutScope
    fun processPaymentAuthRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): ProcessPaymentAuthRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockProcessPaymentAuthRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    @CheckoutScope
    fun paymentAuthTokenRepository(
        testParameters: TestParameters,
        tokensStorage: TokensStorage
    ): PaymentAuthTokenRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            object : PaymentAuthTokenRepository {
                override var paymentAuthToken: String? = "paymentAuthToken"
                override var isUserAccountRemember = false
                override val isPaymentAuthPersisted = true
                override fun persistPaymentAuth() {
                    // does nothing
                }
            }
        } else {
            tokensStorage
        }
    }

    @Provides
    @CheckoutScope
    fun smsSessionRetryRepository(
        testParameters: TestParameters,
        apiV3PaymentAuthRepository: ApiV3PaymentAuthRepository
    ): SmsSessionRetryRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockSmsSessionRetryRepository()
        } else {
            apiV3PaymentAuthRepository
        }
    }

    @Provides
    @CheckoutScope
    fun requestPaymentAuthUseCase(paymentAuthTypeRepository: PaymentAuthTypeRepository): RequestPaymentAuthUseCase {
        return RequestPaymentAuthUseCaseImpl(
            paymentAuthTypeRepository
        )
    }

    @Provides
    @CheckoutScope
    fun processPaymentAuthUseCase(
        processPaymentAuthRepository: ProcessPaymentAuthRepository,
        currentUserRepository: CurrentUserRepository,
        paymentAuthTokenRepository: PaymentAuthTokenRepository,
        errorReporter: ErrorReporter
    ): ProcessPaymentAuthUseCase {
        return ProcessPaymentAuthUseCaseImpl(
            processPaymentAuthRepository,
            currentUserRepository,
            paymentAuthTokenRepository,
            errorReporter
        )
    }

    @[Provides IntoMap ViewModelKey(PAYMENT_AUTH)]
    fun viewModel(
        requestPaymentAuthUseCase: RequestPaymentAuthUseCase,
        processPaymentAuthUseCase: ProcessPaymentAuthUseCase,
        reporter: Reporter
    ): ViewModel {
        return RuntimeViewModel<PaymentAuth.State, PaymentAuth.Action, PaymentAuth.Effect>(
            featureName = "PaymentAuth",
            initial = {
                Out(PaymentAuth.State.Loading) {
                    input { showState(state) }
                }
            },
            logic = {
                PaymentAuthAnalytics(
                    reporter = reporter,
                    businessLogic = PaymentAuthBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        requestPaymentAuthUseCase = requestPaymentAuthUseCase,
                        processPaymentAuthUseCase = processPaymentAuthUseCase
                    )
                )
            }
        )
    }

    companion object {
        const val PAYMENT_AUTH = "PAYMENT_AUTH"
    }
}
