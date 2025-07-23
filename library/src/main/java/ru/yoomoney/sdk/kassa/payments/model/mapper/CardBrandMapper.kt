/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.model.mapper

import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.BankCardType
import ru.yoomoney.sdk.kassa.payments.model.CardBrand

internal fun BankCardType.mapToCardBrand() = when(this) {
    BankCardType.MASTER_CARD -> CardBrand.MASTER_CARD
    BankCardType.VISA -> CardBrand.VISA
    BankCardType.MIR -> CardBrand.MIR
    BankCardType.AMERICAN_EXPRESS -> CardBrand.AMERICAN_EXPRESS
    BankCardType.JCB -> CardBrand.JCB
    BankCardType.CUP -> CardBrand.CUP
    BankCardType.DINERS_CLUB -> CardBrand.DINERS_CLUB
    BankCardType.BANK_CARD -> CardBrand.BANK_CARD
    BankCardType.DISCOVER_CARD -> CardBrand.DISCOVER_CARD
    BankCardType.INSTA_PAYMENT -> CardBrand.INSTA_PAYMENT
    BankCardType.INSTA_PAYMENT_TM -> CardBrand.INSTA_PAYMENT_TM
    BankCardType.LASER -> CardBrand.LASER
    BankCardType.DANKORT -> CardBrand.DANKORT
    BankCardType.SOLO -> CardBrand.SOLO
    BankCardType.SWITCH -> CardBrand.SWITCH
    BankCardType.UNKNOWN -> CardBrand.UNKNOWN
}