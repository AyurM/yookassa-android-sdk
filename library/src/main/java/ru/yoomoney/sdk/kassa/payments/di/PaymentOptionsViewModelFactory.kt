/*
 * The MIT License (MIT)
 * Copyright © 2023 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.extensions.toTokenizeScheme
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ConfigUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionList
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListAnalytics
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListBusinessLogic
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCase
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

internal typealias PaymentOptionsViewModel = RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>

internal class PaymentOptionsViewModelFactory @AssistedInject constructor(
    @Assisted
    private val paymentOptionsAssisted: PaymentOptionsAssisted,
    private val paymentOptionsListUseCase: PaymentOptionsListUseCase,
    private val paymentParameters: PaymentParameters,
    private val reporter: Reporter,
    private val userAuthTypeParamProvider: UserAuthTypeParamProvider,
    private val tokenizeSchemeParamProvider: TokenizeSchemeParamProvider,
    private val logoutUseCase: LogoutUseCase,
    private val getConfirmation: GetConfirmation,
    private val unbindCardUseCase: UnbindCardUseCase,
    private val shopPropertiesRepository: ShopPropertiesRepository,
    private val configUseCase: ConfigUseCase,
    private val configRepository: ConfigRepository,
    private val appContext: Context,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RuntimeViewModel<PaymentOptionList.State, PaymentOptionList.Action, PaymentOptionList.Effect>(
            featureName = PAYMENT_OPTIONS_LIST,
            initial = {
                Out(PaymentOptionList.State.Loading(configRepository.getConfig().yooMoneyLogoUrlLight)) {
                    input { showState(state) }
                    input { configUseCase.loadConfig() }
                }
            },
            logic = {
                PaymentOptionListAnalytics(
                    reporter = reporter,
                    businessLogic = PaymentOptionsListBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        useCase = paymentOptionsListUseCase,
                        logoutUseCase = logoutUseCase,
                        paymentParameters = paymentParameters,
                        paymentMethodId = paymentOptionsAssisted.paymentMethodId,
                        getConfirmation = getConfirmation,
                        unbindCardUseCase = unbindCardUseCase,
                        shopPropertiesRepository = shopPropertiesRepository,
                        configRepository = configRepository,
                        getTokenizeScheme = { paymentOption, paymentInstrument ->
                            paymentOption.toTokenizeScheme(appContext, paymentInstrument)
                        },
                        tokenizeSchemeProvider = tokenizeSchemeParamProvider
                    ),
                    getUserAuthType = userAuthTypeParamProvider,
                    getTokenizeScheme = tokenizeSchemeParamProvider
                )
            }
        ) as T
    }

    @AssistedFactory
    internal interface AssistedPaymentOptionVmFactory {
        fun create(paymentOptionsAssisted: PaymentOptionsAssisted): PaymentOptionsViewModelFactory
    }

    internal class PaymentOptionsAssisted(
        val paymentMethodId: String?
    )

    companion object {
        const val PAYMENT_OPTIONS_LIST = "PaymentOptionList"
    }
}
