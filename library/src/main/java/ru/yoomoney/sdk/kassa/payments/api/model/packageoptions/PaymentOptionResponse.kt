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

package ru.yoomoney.sdk.kassa.payments.api.model.packageoptions

import com.fasterxml.jackson.annotation.JsonProperty

internal sealed class PaymentOptionResponse

internal data class PaymentOptionBankCard(
    @get:JsonProperty("payment_method_type")
    @param:JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodTypeNetwork,

    @get:JsonProperty("confirmation_types")
    @param:JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @get:JsonProperty("save_payment_instrument")
    @param:JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @get:JsonProperty("identification_requirement")
    @param:JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,

    @get:JsonProperty("payment_instruments")
    @param:JsonProperty("payment_instruments")
    val paymentInstruments: List<PaymentInstrumentBankCard>?

) : PaymentOptionResponse()

internal sealed class PaymentInstrumentYooMoney : PaymentOptionResponse() {

    data class PaymentInstrumentYooMoneyWallet(

        @get:JsonProperty("payment_method_type")
        @param:JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodTypeNetwork,

        @get:JsonProperty("confirmation_types")
        @param:JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @get:JsonProperty("save_payment_method")
        @param:JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @get:JsonProperty("save_payment_instrument")
        @param:JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean,

        @get:JsonProperty("identification_requirement")
        @param:JsonProperty("identification_requirement")
        val identificationRequirement: IdentificationRequirement?,

        @get:JsonProperty("instrument_type")
        @param:JsonProperty("instrument_type")
        val instrumentType: InstrumentType,

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("balance")
        val balance: Amount?,
    ) : PaymentInstrumentYooMoney()

    data class PaymentInstrumentYooMoneyLinkedBankCard(

        @get:JsonProperty("payment_method_type")
        @param:JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodTypeNetwork,

        @get:JsonProperty("confirmation_types")
        @param:JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @get:JsonProperty("save_payment_method")
        @param:JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @get:JsonProperty("save_payment_instrument")
        @param:JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean,

        @get:JsonProperty("identification_requirement")
        @param:JsonProperty("identification_requirement")
        val identificationRequirement: IdentificationRequirement?,

        @get:JsonProperty("instrument_type")
        @param:JsonProperty("instrument_type")
        val instrumentType: InstrumentType,

        @JsonProperty("id")
        val id: String,

        @get:JsonProperty("card_name")
        @param:JsonProperty("card_name")
        val cardName: String?,

        @get:JsonProperty("card_mask")
        @param:JsonProperty("card_mask")
        val cardMask: String,

        @get:JsonProperty("card_type")
        @param:JsonProperty("card_type")
        val cardType: BankCardType

    ) : PaymentInstrumentYooMoney()

    data class AbstractYooMoneyWallet(

        @get:JsonProperty("payment_method_type")
        @param:JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodTypeNetwork,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @get:JsonProperty("save_payment_method")
        @param:JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @get:JsonProperty("confirmation_types")
        @param:JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @get:JsonProperty("save_payment_instrument")
        @param:JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean
    ) : PaymentInstrumentYooMoney()
}

internal data class PaymentOptionSberbank(

    @get:JsonProperty("payment_method_type")
    @param:JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodTypeNetwork,

    @get:JsonProperty("confirmation_types")
    @param:JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @get:JsonProperty("fee")
    @param:JsonProperty("fee")
    val fee: Fee?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @get:JsonProperty("save_payment_instrument")
    @param:JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @get:JsonProperty("identification_requirement")
    @param:JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,
) : PaymentOptionResponse()

internal data class PaymentOptionGooglePay(
    @get:JsonProperty("payment_method_type")
    @param:JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodTypeNetwork,

    @get:JsonProperty("confirmation_types")
    @param:JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @get:JsonProperty("save_payment_instrument")
    @param:JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @get:JsonProperty("identification_requirement")
    @param:JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,

    @get:JsonProperty("payment_systems_allowed")
    @param:JsonProperty("payment_systems_allowed")
    val paymentSystemsAllowed: BankCardType,
) : PaymentOptionResponse()

internal data class PaymentOptionSBP(
    @get:JsonProperty("payment_method_type")
    @param:JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodTypeNetwork,

    @get:JsonProperty("confirmation_types")
    @param:JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @get:JsonProperty("save_payment_method")
    @param:JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @get:JsonProperty("save_payment_instrument")
    @param:JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean

) : PaymentOptionResponse()

internal object Unknown : PaymentOptionResponse()