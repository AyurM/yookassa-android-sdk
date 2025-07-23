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

package ru.yoomoney.sdk.kassa.payments.api.model.config

import com.fasterxml.jackson.annotation.JsonProperty

internal data class SavePaymentMethodOptionTextsResponse(
    @JsonProperty("switchRecurrentOnBindOnTitle")
    val switchRecurrentOnBindOnTitle: String,

    @JsonProperty("switchRecurrentOnBindOnSubtitle")
    val switchRecurrentOnBindOnSubtitle: String,

    @JsonProperty("switchRecurrentOnBindOffTitle")
    val switchRecurrentOnBindOffTitle: String,

    @JsonProperty("switchRecurrentOnBindOffSubtitle")
    val switchRecurrentOnBindOffSubtitle: String,

    @JsonProperty("switchRecurrentOffBindOnTitle")
    val switchRecurrentOffBindOnTitle: String,

    @JsonProperty("switchRecurrentOffBindOnSubtitle")
    val switchRecurrentOffBindOnSubtitle: String,

    @JsonProperty("messageRecurrentOnBindOnTitle")
    val messageRecurrentOnBindOnTitle: String,

    @JsonProperty("messageRecurrentOnBindOnSubtitle")
    val messageRecurrentOnBindOnSubtitle: String,

    @JsonProperty("messageRecurrentOnBindOffTitle")
    val messageRecurrentOnBindOffTitle: String,

    @JsonProperty("messageRecurrentOnBindOffSubtitle")
    val messageRecurrentOnBindOffSubtitle: String,

    @JsonProperty("messageRecurrentOffBindOnTitle")
    val messageRecurrentOffBindOnTitle: String,

    @JsonProperty("messageRecurrentOffBindOnSubtitle")
    val messageRecurrentOffBindOnSubtitle: String,

    @JsonProperty("screenRecurrentOnBindOnTitle")
    val screenRecurrentOnBindOnTitle: String,

    @JsonProperty("screenRecurrentOnBindOnText")
    val screenRecurrentOnBindOnText: String,

    @JsonProperty("screenRecurrentOnBindOffTitle")
    val screenRecurrentOnBindOffTitle: String,

    @JsonProperty("screenRecurrentOnBindOffText")
    val screenRecurrentOnBindOffText: String,

    @JsonProperty("screenRecurrentOffBindOnTitle")
    val screenRecurrentOffBindOnTitle: String,

    @JsonProperty("screenRecurrentOffBindOnText")
    val screenRecurrentOffBindOnText: String,

    @JsonProperty("screenRecurrentOnSberpayTitle")
    val screenRecurrentOnSberpayTitle: String,

    @JsonProperty("screenRecurrentOnSberpayText")
    val screenRecurrentOnSberpayText: String,

    @JsonProperty("messageRecurrentOnSBPTitle")
    val messageRecurrentOnSBPTitle: String,

    @JsonProperty("switchRecurrentOnSBPTitle")
    val switchRecurrentOnSBPTitle: String,

    @JsonProperty("messageRecurrentOnSberPayTitle")
    val messageRecurrentOnSberPayTitle: String,

    @JsonProperty("switchRecurrentOnSberPayTitle")
    val switchRecurrentOnSberPayTitle: String
)