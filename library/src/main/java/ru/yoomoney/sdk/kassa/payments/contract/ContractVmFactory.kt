/*
 * The MIT License (MIT)
 * Copyright © 2024 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.contract

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule.Companion.CONTRACT
import ru.yoomoney.sdk.kassa.payments.extensions.toTokenizeScheme
import ru.yoomoney.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.TokenizeSchemeParamProvider
import ru.yoomoney.sdk.kassa.payments.metrics.UserAuthTypeParamProvider
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

internal class ContractVmFactory @AssistedInject constructor(
    private val context: Context,
    private val selectPaymentMethodUseCase: SelectPaymentMethodUseCase,
    private val paymentParameters: PaymentParameters,
    private val logoutUseCase: LogoutUseCase,
    private val reporter: Reporter,
    private val userAuthTypeParamProvider: UserAuthTypeParamProvider,
    private val getConfirmation: GetConfirmation,
    private val loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    private val userAuthInfoRepository: TokensStorage,
    private val tokenizeSchemeParamProvider: TokenizeSchemeParamProvider,
    private val shopPropertiesRepository: ShopPropertiesRepository,
    private val configRepository: ConfigRepository,
    @Assisted
    private val paymentOptionId: Int,
    @Assisted
    private val instrumentId: String?,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RuntimeViewModel<Contract.State, Contract.Action, Contract.Effect>(
            featureName = CONTRACT,
            initial = {
                Out(Contract.State.Loading) {
                    input { showState(state) }
                }
            },
            logic = {
                ContractAnalytics(
                    reporter = reporter,
                    businessLogic = ContractBusinessLogic(
                        paymentParameters = paymentParameters,
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        selectPaymentMethodUseCase = selectPaymentMethodUseCase,
                        logoutUseCase = logoutUseCase,
                        getConfirmation = getConfirmation,
                        loadedPaymentOptionListRepository = loadedPaymentOptionListRepository,
                        shopPropertiesRepository = shopPropertiesRepository,
                        userAuthInfoRepository = userAuthInfoRepository,
                        configRepository = configRepository,
                        defaultAgentSchemeUserAgreementUrl = getDefaultAgentSchemeUserAgreementUrl(context),
                        paymentOptionId = paymentOptionId,
                        instrumentId = instrumentId
                    ),
                    tokenizeSchemeParamProvider = tokenizeSchemeParamProvider,
                    getUserAuthType = userAuthTypeParamProvider,
                    paymentParameters = paymentParameters,
                    getTokenizeScheme = { paymentOption, paymentInstrument ->
                        paymentOption.toTokenizeScheme(context, paymentInstrument)
                    }
                )
            }
        ) as T
    }

    @AssistedFactory
    internal interface AssistedContractVMFactory {
        fun create(paymentOptionId: Int, instrumentId: String?): ContractVmFactory
    }
}