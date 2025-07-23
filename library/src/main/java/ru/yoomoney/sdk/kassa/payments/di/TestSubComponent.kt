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
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.di.module.OkHttpModule
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentOptionsListModule
import ru.yoomoney.sdk.kassa.payments.di.module.TokensStorageModule

internal fun setComponent(
    context: Context,
    paymentParameters: PaymentParameters,
    testParameters: TestParameters,
    uiParameters: UiParameters,
    okHttpModule: OkHttpModule,
    paymentOptionsListModule: PaymentOptionsListModule,
    tokensStorageModule: TokensStorageModule,
) {
    CheckoutInjector.setupComponent(
        isConfirmation = false,
        context = context,
        paymentParameters = paymentParameters,
        testParameters = testParameters,
        uiParameters = uiParameters,
        okHttpModule = okHttpModule,
        paymentOptionsListModule = paymentOptionsListModule,
        tokensStorageModule = tokensStorageModule,
    )
}