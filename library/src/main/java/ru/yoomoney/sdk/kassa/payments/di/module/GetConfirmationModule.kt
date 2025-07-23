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

package ru.yoomoney.sdk.kassa.payments.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.di.scope.CheckoutScope
import ru.yoomoney.sdk.kassa.payments.extensions.getConfirmation
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.GetConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.utils.DEFAULT_REDIRECT_URL

@Module
internal class GetConfirmationModule {

    @Provides
    @CheckoutScope
    fun getConfirmation(
        context: Context,
        paymentParameters: PaymentParameters,
    ): GetConfirmation {
        return object : GetConfirmation {
            override fun invoke(paymentOption: PaymentOption): Confirmation {
                return paymentOption.getConfirmation(
                    context = context,
                    returnUrl =  paymentParameters.customReturnUrl ?: DEFAULT_REDIRECT_URL,
                    appScheme = context.resources.getString(R.string.ym_app_scheme),
                )
            }
        }
    }
}