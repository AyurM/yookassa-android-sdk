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

package ru.yoomoney.sdk.kassa.payments.unbind.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.scope.CheckoutScope
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.unbindCard.UnbindCardGateway
import ru.yoomoney.sdk.kassa.payments.paymentMethodInfo.UnbindCardMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.unbind.impl.MockUnbindCardGateway
import ru.yoomoney.sdk.kassa.payments.unbind.impl.UnbindBusinessLogic
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardContract
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardAnalytics
import ru.yoomoney.sdk.kassa.payments.unbind.impl.UnbindCardUseCase
import ru.yoomoney.sdk.kassa.payments.unbind.impl.UnbindCardUseCaseImpl
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel

@Module
internal class UnbindCardModule {

    @Provides
    @CheckoutScope
    fun unbindCardMethodInfoGatewayProvider(
        testParameters: TestParameters,
        paymentsApi: PaymentsApi
    ): UnbindCardGateway {
        return if (testParameters.mockConfiguration != null) {
            MockUnbindCardGateway()
        } else {
            UnbindCardMethodInfoGateway(paymentsApi)
        }
    }

    @Provides
    @CheckoutScope
    fun unbindCardUseCaseProvider(
        unbindCardInfoGateway: UnbindCardGateway,
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository
    ): UnbindCardUseCase {
        return UnbindCardUseCaseImpl(unbindCardInfoGateway, getLoadedPaymentOptionListRepository)
    }

    @[Provides IntoMap ViewModelKey(UNBIND_CARD)]
    fun viewModel(
        reporter: Reporter,
        unbindCardUseCase: UnbindCardUseCase
    ): ViewModel {
        return RuntimeViewModel<UnbindCardContract.State, UnbindCardContract.Action, UnbindCardContract.Effect>(
            featureName = UNBIND_CARD,
            initial = { Out.skip(UnbindCardContract.State.Initial, source) },
            logic = {
                UnbindCardAnalytics(
                    businessLogic = UnbindBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        unbindCardUseCase = unbindCardUseCase
                    ),
                    reporter = reporter
                )
            }
        )
    }

    companion object {
        const val UNBIND_CARD = "UNBIND_CARD"
    }
}