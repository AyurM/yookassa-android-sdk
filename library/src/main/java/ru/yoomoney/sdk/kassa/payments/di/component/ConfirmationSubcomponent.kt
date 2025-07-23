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

import dagger.Subcomponent
import ru.yoomoney.sdk.kassa.payments.confirmation.ConfirmationActivity
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.ui.SBPConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentDetailsModule
import ru.yoomoney.sdk.kassa.payments.di.module.SbpModule
import ru.yoomoney.sdk.kassa.payments.di.scope.ConfirmationScope
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui.BankListFragment

@ConfirmationScope
@Subcomponent(modules = [SbpModule::class, PaymentDetailsModule::class])
internal interface ConfirmationSubcomponent {

    fun inject(activity: ConfirmationActivity)

    fun inject(confirmationFragment: SBPConfirmationFragment)

    fun inject(confirmationFragment: SberPayConfirmationFragment)

    fun inject(fragment: BankListFragment)

    @Subcomponent.Builder
    interface Builder {

        fun build(): ConfirmationSubcomponent
    }
}