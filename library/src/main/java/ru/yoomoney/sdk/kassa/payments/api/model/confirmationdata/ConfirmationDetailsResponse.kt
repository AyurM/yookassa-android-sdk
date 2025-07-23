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

package ru.yoomoney.sdk.kassa.payments.api.model.confirmationdata

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

internal data class ConfirmationDetailsResponse(
    @JsonProperty("payment_id")
    val paymentId: String,

    @JsonProperty("confirmation_data")
    val confirmationData: ConfirmationDataResponse,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = ConfirmationDataResponse.UnknownConfirmationDataResponse::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfirmationDataResponse.SbpConfirmationDataResponse::class, name = "sbp"),
    JsonSubTypes.Type(value = ConfirmationDataResponse.SberPayConfirmationDataResponse::class, name = "sberbank"),
)
internal sealed interface ConfirmationDataResponse {

    data class SbpConfirmationDataResponse(
        @JsonProperty("url")
        val url: String
    ) : ConfirmationDataResponse

    data class SberPayConfirmationDataResponse(
        @JsonProperty("merchant_login")
        val merchantLogin: String,

        @JsonProperty("order_id")
        val orderId: String,

        @JsonProperty("order_number")
        val orderNumber: String,

        @JsonProperty("api_key")
        val apiKey: String?
    ) : ConfirmationDataResponse

    data object UnknownConfirmationDataResponse : ConfirmationDataResponse
}
