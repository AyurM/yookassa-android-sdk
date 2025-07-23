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

internal data class PaymentSystemInfo(
    val id: String,
    val bins: List<String>,
    @DrawableRes val icon: Int
)

internal fun initBanks(): List<PaymentSystemInfo> {
    return listOf(
        PaymentSystemInfo(
            "visa",
            listOf("40", "41", "42", "43", "44", "45", "46", "47", "48", "49"),
            R.drawable.ym_ic_card_type_visa_l
        ),
        PaymentSystemInfo(
            "union_pay",
            listOf("62", "81"),
            R.drawable.ym_ic_cardbrand_cup
        ),
        PaymentSystemInfo(
            "mir",
            listOf("22", "35", "94", "90","91", "99"),
            R.drawable.ym_ic_cardbrand_mir
        ),
        PaymentSystemInfo(
            "mastercard",
            listOf("67", "50", "51", "52", "53", "54", "55", "56","58", "63"),
            R.drawable.ym_ic_card_type_mc_l
        ),
        PaymentSystemInfo(
            "unknown",
            listOf(),
            R.drawable.ym_ic_unknown_list
        )
    )
}