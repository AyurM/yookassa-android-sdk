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

package ru.yoomoney.sdk.kassa.payments.model.mapper

import ru.yoomoney.sdk.kassa.payments.api.model.paymentmethod.PaymentMethodResponse
import ru.yoomoney.sdk.kassa.payments.model.PaymentMethodBankCard
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentMethodTypeNetwork
import ru.yoomoney.sdk.kassa.payments.api.model.paymentmethod.CardInfoResponse
import ru.yoomoney.sdk.kassa.payments.api.model.paymentmethod.PaymentMethodBankCardResponse
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.model.CardInfo

internal fun PaymentMethodResponse.mapToPaymentMethodBankCard(): PaymentMethodBankCard? = when (this) {
    is PaymentMethodBankCardResponse -> { this.mapToPaymentMethodBankCard() }
    else -> null
}

private fun PaymentMethodBankCardResponse.mapToPaymentMethodBankCard(): PaymentMethodBankCard {
    val paymentType = this.type.mapToPaymentMethodType()
    return PaymentMethodBankCard(
        type = paymentType,
        id = id,
        saved = saved,
        cscRequired = cscRequired,
        title = title,
        card = card?.mapToCardInfo(paymentType)
    )
}

    private fun PaymentMethodTypeNetwork.mapToPaymentMethodType(): PaymentMethodType = when(this) {
        PaymentMethodTypeNetwork.BANK_CARD -> PaymentMethodType.BANK_CARD
        PaymentMethodTypeNetwork.GOOGLE_PAY -> PaymentMethodType.GOOGLE_PAY
        PaymentMethodTypeNetwork.SBERBANK -> PaymentMethodType.SBERBANK
        PaymentMethodTypeNetwork.YOO_MONEY -> PaymentMethodType.YOO_MONEY
        PaymentMethodTypeNetwork.SBP -> PaymentMethodType.SBP
        else -> {
            throw IllegalAccessError("")
        }
    }

    private fun CardInfoResponse.mapToCardInfo(type: PaymentMethodType) = CardInfo(
        first = first6,
        last = last4,
        expiryYear = expiryYear,
        expiryMonth = expiryMonth,
        cardType = cardType.mapToCardBrand(),
        source = type
    )

