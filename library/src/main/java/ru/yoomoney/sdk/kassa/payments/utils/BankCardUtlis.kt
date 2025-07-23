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

package ru.yoomoney.sdk.kassa.payments.utils

import androidx.annotation.DrawableRes
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.extensions.getIconResId
import ru.yoomoney.sdk.kassa.payments.model.CardBrand

internal var UNKNOWN_CARD_ICON = R.drawable.ym_ic_unknown_list

private val banks by lazy { initBanks() }

@DrawableRes
internal fun getBankOrBrandLogo(pan: String, brand: CardBrand): Int {
    return getBankLogo(pan).let {
        it.takeIf { it != UNKNOWN_CARD_ICON} ?: brand.getIconResId()
    }
}

@DrawableRes
internal fun getBankLogo(pan: String): Int {
    return getPaymentMethodLogo(pan)
}

@DrawableRes
private fun getPaymentMethodLogo(pan: String): Int {
    val panInput = pan.replace(" ", "").take(2)
    var icon = UNKNOWN_CARD_ICON
    banks.forEach { bank ->
        bank.bins.find { it == panInput }?.let {
            icon = bank.icon
            return@forEach
        }
    }
    return icon
}