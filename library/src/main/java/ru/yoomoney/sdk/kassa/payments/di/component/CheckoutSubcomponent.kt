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

package ru.yoomoney.sdk.kassa.payments.di.component

import dagger.BindsInstance
import dagger.Subcomponent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule
import ru.yoomoney.sdk.kassa.payments.di.scope.CheckoutScope
import ru.yoomoney.sdk.kassa.payments.di.module.ErrorReporterModule
import ru.yoomoney.sdk.kassa.payments.di.module.GetConfirmationModule
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentModule
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentOptionsListModule
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentAuth.di.PaymentAuthModule
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.ui.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.di.TokenizeModule
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoomoney.sdk.kassa.payments.ui.view.BankCardView
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.unbind.di.UnbindCardModule
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.di.UserAuthModule

@CheckoutScope
@Subcomponent(
    modules = [
        TokenizeModule::class,
        GetConfirmationModule::class,
        ContractModule::class,
        PaymentModule::class,
        UserAuthModule::class,
        PaymentOptionsListModule::class,
        PaymentAuthModule::class,
        UnbindCardModule::class,
        ErrorReporterModule::class
    ]
)
internal interface CheckoutSubcomponent {

    fun inject(activity: CheckoutActivity)

    fun inject(paymentOptionListFragment: PaymentOptionListFragment)

    fun inject(contractFragment: ContractFragment)

    fun inject(contractFragment: TokenizeFragment)

    fun inject(unbindCardFragment: UnbindCardFragment)

    fun inject(paymentAuthFragment: PaymentAuthFragment)

    fun inject(moneyAuthFragment: MoneyAuthFragment)

    fun inject(bankCardView: BankCardView)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun paymentParameters(paymentParameters: PaymentParameters): Builder

        @BindsInstance
        fun uiParameters(uiParameters: UiParameters): Builder

        fun paymentOptionsListModule(paymentOptionsListModule: PaymentOptionsListModule): Builder

        fun build(): CheckoutSubcomponent
    }
}
