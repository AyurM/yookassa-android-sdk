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

package ru.yoomoney.sdk.kassa.payments.api.model.tokens

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.Amount

internal data class TokensRequestPaymentMethodData(

    @JsonProperty("amount")
    val amount: Amount?,

    @get:JsonProperty("tmx_session_id")
    @param:JsonProperty("tmx_session_id")
    val tmxSessionId: String,

    @JsonProperty("confirmation")
    val confirmation: ConfirmationRequest?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: Boolean?,

    @get:JsonProperty("payment_method_data")
    @param:JsonProperty("payment_method_data")
    val paymentMethodData: PaymentMethodDataRequest,

    @get:JsonProperty("merchant_customer_id")
    @param:JsonProperty("merchant_customer_id")
    val merchantCustomerId: String?,

    @get:JsonProperty("save_payment_instrument")
    @param:JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean?,
)

internal data class TokensRequestPaymentInstrumentId(

    @JsonProperty("amount")
    val amount: Amount?,

    @get:JsonProperty("tmx_session_id")
    @param:JsonProperty("tmx_session_id")
    val tmxSessionId: String,

    @JsonProperty("confirmation")
    val confirmation: ConfirmationRequest?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: Boolean?,

    @get:JsonProperty("payment_instrument_id")
    @param:JsonProperty("payment_instrument_id")
    val paymentInstrumentId: String,

    @JsonProperty("csc")
    val csc: String?
)

internal data class TokensRequestPaymentMethodId(

    @JsonProperty("amount")
    val amount: Amount?,

    @get:JsonProperty("tmx_session_id")
    @param:JsonProperty("tmx_session_id")
    val tmxSessionId: String,

    @JsonProperty("confirmation")
    val confirmation: ConfirmationRequest?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: Boolean?,

    @get:JsonProperty("payment_method_id")
    @param:JsonProperty("payment_method_id")
    val paymentMethodId: String,

    @JsonProperty("csc")
    val csc: String?
)
