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
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.InstrumentType
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentMethodTypeNetwork

sealed class PaymentMethodDataRequest

internal data class PaymentMethodDataBankCardRequest(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @JsonProperty("card")
    val card: CardRequest?,
) : PaymentMethodDataRequest()

internal data class PaymentMethodDataYooMoneyWalletRequest(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @get:JsonProperty("instrument_type")
    @param:JsonProperty("instrument_type")
    val instrumentType: InstrumentType
): PaymentMethodDataRequest()

internal data class PaymentMethodDataYooMoneyLinkedBankCardRequest(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @get:JsonProperty("instrument_type")
    @param:JsonProperty("instrument_type")
    val instrumentType: InstrumentType,

    @JsonProperty("csc")
    val csc: String
): PaymentMethodDataRequest()

internal data class PaymentMethodDataSberbankRequest(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @JsonProperty("phone")
    val phone: String?
) : PaymentMethodDataRequest()

internal data class PaymentMethodDataGooglePayRequest(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork,

    @get:JsonProperty("payment_method_token")
    @param:JsonProperty("payment_method_token")
    val paymentMethodToken: String
) : PaymentMethodDataRequest()

internal data class PaymentMethodDataSBPRequest(
    @JsonProperty("type")
    val type: PaymentMethodTypeNetwork
) : PaymentMethodDataRequest()
