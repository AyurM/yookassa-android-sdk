/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.confirmation.ConfirmationActivity
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.ui.SBPConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.di.component.CheckoutSubcomponent
import ru.yoomoney.sdk.kassa.payments.di.component.ConfirmationSubcomponent
import ru.yoomoney.sdk.kassa.payments.di.component.DaggerMainComponent
import ru.yoomoney.sdk.kassa.payments.di.component.MainComponent
import ru.yoomoney.sdk.kassa.payments.di.module.OkHttpModule
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentOptionsListModule
import ru.yoomoney.sdk.kassa.payments.di.module.TokensStorageModule
import ru.yoomoney.sdk.kassa.payments.extensions.initAppendableExtensions
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui.BankListFragment
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.ui.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoomoney.sdk.kassa.payments.ui.view.BankCardView
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.utils.checkUrl
import ru.yoomoney.sdk.kassa.payments.utils.getAllPaymentMethods

internal object CheckoutInjector {

    private lateinit var component: MainComponent

    private lateinit var checkoutComponent: CheckoutSubcomponent

    private lateinit var confirmationSubcomponent: ConfirmationSubcomponent

    fun setupComponent(
        isConfirmation: Boolean,
        context: Context,
        clientApplicationKey: String? = null,
        testParameters: TestParameters = TestParameters(),
        uiParameters: UiParameters = UiParameters(),
        paymentParameters: PaymentParameters? = null,
        okHttpModule: OkHttpModule = OkHttpModule(),
        paymentOptionsListModule: PaymentOptionsListModule = PaymentOptionsListModule(),
        tokensStorageModule: TokensStorageModule = TokensStorageModule(),
    ) {
        val shopParameters = when {
            paymentParameters != null &&
                    paymentParameters.paymentMethodTypes.isEmpty()
                    && clientApplicationKey.isNullOrEmpty() -> {
                paymentParameters.copy(paymentMethodTypes = getAllPaymentMethods())
            }
            paymentParameters != null && clientApplicationKey.isNullOrEmpty() -> paymentParameters
            else -> null
        }
        shopParameters?.customReturnUrl?.let { checkUrl(it) }

        InMemoryColorSchemeRepository.colorScheme = uiParameters.colorScheme

        initAppendableExtensions(context)

        component = DaggerMainComponent.builder()
            .context(context.applicationContext)
            .okHttpModule(okHttpModule)
            .tokensStorageModule(tokensStorageModule)
            .testParameters(testParameters)
            .clientApplicationKey(clientApplicationKey ?: paymentParameters?.clientApplicationKey.orEmpty())
            .build()

        if (isConfirmation) {
            confirmationSubcomponent = component.appendConfirmationSubcomponent()
                .build()
        } else {
            checkoutComponent = component.appendCheckoutSubcomponent()
                .paymentParameters(paymentParameters!!)
                .paymentOptionsListModule(paymentOptionsListModule)
                .uiParameters(uiParameters)
                .build()
        }
    }

    fun injectCheckoutActivity(activity: CheckoutActivity) {
        checkoutComponent.inject(activity)
    }

    fun injectMainDialogFragment(fragment: MainDialogFragment) {
        component.inject(fragment)
    }

    fun injectContractFragment(fragment: ContractFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectUntieCardFragment(fragment: UnbindCardFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectTokenizeFragment(fragment: TokenizeFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectPaymentAuthFragment(fragment: PaymentAuthFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectPaymentOptionListFragment(fragment: PaymentOptionListFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectMoneyAuthFragment(fragment: MoneyAuthFragment) {
        checkoutComponent.inject(fragment)
    }

    fun injectBankCardView(bankCardView: BankCardView) {
        checkoutComponent.inject(bankCardView)
    }

    fun injectConfirmationActivity(activity: ConfirmationActivity) {
        confirmationSubcomponent.inject(activity)
    }

    fun injectBankListFragment(fragment: BankListFragment) {
        confirmationSubcomponent.inject(fragment)
    }

    fun injectConfirmationFragment(fragment: SBPConfirmationFragment) {
        confirmationSubcomponent.inject(fragment)
    }

    fun injectConfirmationFragment(fragment: SberPayConfirmationFragment) {
        confirmationSubcomponent.inject(fragment)
    }
}
