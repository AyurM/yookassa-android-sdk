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

package ru.yoomoney.sdk.kassa.payments.api

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.InstrumentType
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentInstrumentYooMoney
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentMethodTypeNetwork
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionBankCard
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionGooglePay
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionResponse
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionSBP
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionSberbank
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.Unknown

internal class PaymentOptionDeserializer : JsonDeserializer<PaymentOptionResponse>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): PaymentOptionResponse {
        val node: JsonNode = jsonParser.readValueAsTree()
        val codec = jsonParser.codec
        return when (codec.treeToValue(node.get("payment_method_type"), PaymentMethodTypeNetwork::class.java)) {
            PaymentMethodTypeNetwork.YOO_MONEY -> parseYooWallet(node, jsonParser)
            PaymentMethodTypeNetwork.BANK_CARD -> codec.treeToValue(node, PaymentOptionBankCard::class.java)
            PaymentMethodTypeNetwork.SBERBANK -> codec.treeToValue(node, PaymentOptionSberbank::class.java)
            PaymentMethodTypeNetwork.GOOGLE_PAY -> codec.treeToValue(node, PaymentOptionGooglePay::class.java)
            PaymentMethodTypeNetwork.SBP -> codec.treeToValue(node, PaymentOptionSBP::class.java)
            PaymentMethodTypeNetwork.UNKNOWN -> Unknown
            else -> Unknown
        }
    }

    private fun parseYooWallet(node: JsonNode, jsonParser: JsonParser): PaymentOptionResponse {
        return if (node.has("instrument_type")) {
            val instrumentType = jsonParser.codec.treeToValue(node.get("instrument_type"), InstrumentType::class.java)
            when (instrumentType) {
                InstrumentType.WALLET -> {
                    jsonParser.codec.treeToValue(
                        node,
                        PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyWallet::class.java
                    )
                }
                InstrumentType.LINKED_BANK_CARD -> {
                    jsonParser.codec.treeToValue(
                        node,
                        PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyLinkedBankCard::class.java
                    )
                }
            }
        } else {
            jsonParser.codec.treeToValue(node, PaymentInstrumentYooMoney.AbstractYooMoneyWallet::class.java)
        }
    }
}