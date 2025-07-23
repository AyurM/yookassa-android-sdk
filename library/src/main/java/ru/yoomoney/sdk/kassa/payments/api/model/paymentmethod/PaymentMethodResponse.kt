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

package ru.yoomoney.sdk.kassa.payments.api.model.paymentmethod

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.BankCardType
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentMethodTypeNetwork

internal sealed class PaymentMethodResponse

internal data class PaymentMethodBankCardResponse(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("saved")
    val saved: Boolean,

    @JsonProperty("title")
    val title: String?,

    @get:JsonProperty("csc_required")
    @param:JsonProperty("csc_required")
    val cscRequired: Boolean,

    @JsonProperty("card")
    val card: CardInfoResponse?
): PaymentMethodResponse()

internal object Unknown: PaymentMethodResponse()

internal data class CardInfoResponse(
    @JsonProperty("first6")
    val first6: String?,

    @JsonProperty("last4")
    val last4: String,

    @get:JsonProperty("expiry_year")
    @param:JsonProperty("expiry_year")
    val expiryYear: String,

    @get:JsonProperty("expiry_month")
    @param:JsonProperty("expiry_month")
    val expiryMonth: String,

    @get:JsonProperty("card_type")
    @param:JsonProperty("card_type")
    val cardType: BankCardType,

    @JsonProperty("source")
    val source: BankCardSourceResponse?
)

internal enum class BankCardSourceResponse {
    @JsonProperty("apple_pay")
    APPLE_PAY,

    @JsonProperty("google_pay")
    GOOGLE_PAY,

    @JsonEnumDefaultValue
    UNKNOWN
}
