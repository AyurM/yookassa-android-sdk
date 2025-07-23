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

package ru.yoomoney.sdk.kassa.payments.contract.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.auth.YooMoneyAuth
import ru.yoomoney.sdk.auth.api.account.AccountRepository
import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.contract.SelectPaymentMethodUseCase
import ru.yoomoney.sdk.kassa.payments.contract.SelectPaymentMethodUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.di.module.TokenStorageModule
import ru.yoomoney.sdk.kassa.payments.di.scope.CheckoutScope
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepository
import ru.yoomoney.sdk.kassa.payments.logout.LogoutRepositoryImpl
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.ApiV3TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.MockTokenizeRepository
import ru.yoomoney.sdk.kassa.payments.payment.tokenize.TokenizeRepository
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.secure.BcKeyStorage
import ru.yoomoney.sdk.kassa.payments.secure.Decrypter
import ru.yoomoney.sdk.kassa.payments.secure.Encrypter
import ru.yoomoney.sdk.kassa.payments.secure.SharedPreferencesIvStorage
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.yooprofiler.YooProfiler

@Module
internal class ContractModule {

    @Provides
    @CheckoutScope
    fun provideTokenizeRepository(
        testParameters: TestParameters,
        paymentParameters: PaymentParameters,
        profiler: YooProfiler,
        paymentsApi: PaymentsApi,
        paymentAuthTokenRepository: PaymentAuthTokenRepository,
        profilingSessionIdStorage: ProfilingSessionIdStorage
    ): TokenizeRepository {
        val mockConfiguration = testParameters.mockConfiguration
        return if (mockConfiguration != null) {
            MockTokenizeRepository(mockConfiguration.completeWithError)
        } else {
            ApiV3TokenizeRepository(
                paymentsApi = paymentsApi,
                profiler = profiler,
                profilingSessionIdStorage = profilingSessionIdStorage,
                merchantCustomerId = paymentParameters.customerId,
                paymentAuthTokenRepository = paymentAuthTokenRepository
            )
        }
    }

    @Provides
    @CheckoutScope
    fun selectPaymentOptionsUseCase(
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway,
        accountRepository: AccountRepository,
        userAuthInfoRepository: TokensStorage,
    ): SelectPaymentMethodUseCase {
        return SelectPaymentMethodUseCaseImpl(
            getLoadedPaymentOptionListRepository,
            checkPaymentAuthRequiredGateway,
            accountRepository,
            userAuthInfoRepository,
        )
    }

    @Provides
    @CheckoutScope
    fun provideLogoutRepository(
        context: Context,
        hostProvider: HostProvider,
        currentUserRepository: CurrentUserRepository,
        userAuthInfoRepository: TokensStorage,
        paymentAuthTokenRepository: PaymentAuthTokenRepository,
        profilingSessionIdStorage: ProfilingSessionIdStorage,
        paymentParameters: PaymentParameters,
        ivStorage: SharedPreferencesIvStorage,
        encrypt: Encrypter,
        keyStorage: BcKeyStorage,
        decrypt: Decrypter,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): LogoutRepository {
        return LogoutRepositoryImpl(
            currentUserRepository = currentUserRepository,
            userAuthInfoRepository = userAuthInfoRepository,
            paymentAuthTokenRepository = paymentAuthTokenRepository,
            profilingSessionIdStorage = profilingSessionIdStorage,
            removeKeys = {
                ivStorage.remove(TokenStorageModule.ivKey)
                keyStorage.remove(TokenStorageModule.keyKey)
                encrypt.reset()
                decrypt.reset()
            },
            revokeUserAuthToken = { token ->
                if (token != null && paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY)) {
                    YooMoneyAuth.logout(context.applicationContext, token, hostProvider.host())
                }
            },
            loadedPaymentOptionListRepository = loadedPaymentOptionListRepository
        )
    }

    @Provides
    @CheckoutScope
    fun logoutUseCase(
        logoutRepository: LogoutRepository
    ): LogoutUseCase {
        return LogoutUseCaseImpl(logoutRepository)
    }

    companion object {
        const val CONTRACT = "CONTRACT"
    }
}