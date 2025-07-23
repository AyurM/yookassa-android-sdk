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

import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.InstrumentType
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentMethodTypeNetwork
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.CardRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.ConfirmationAttrsExternalRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.ConfirmationAttrsMobileApplicationRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.ConfirmationAttrsRedirectRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.ConfirmationRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataBankCardRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataGooglePayRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataSBPRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataSberbankRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataYooMoneyLinkedBankCardRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.PaymentMethodDataYooMoneyWalletRequest
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensResponse
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.model.GooglePayInfo
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.NewCardInfo
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.PaymentTokenInfo
import ru.yoomoney.sdk.kassa.payments.model.ProfilingInfo
import ru.yoomoney.sdk.kassa.payments.model.RedirectConfirmation
import ru.yoomoney.sdk.kassa.payments.model.SBPInfo
import ru.yoomoney.sdk.kassa.payments.model.SberPay
import ru.yoomoney.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoomoney.sdk.kassa.payments.model.WalletInfo

internal fun Confirmation.toRequest(): ConfirmationRequest = when (this) {
    is ExternalConfirmation -> ConfirmationAttrsExternalRequest(type = ConfirmationType.EXTERNAL)
    is RedirectConfirmation -> ConfirmationAttrsRedirectRequest(type = ConfirmationType.REDIRECT, returnUrl = returnUrl)
    is MobileApplication -> ConfirmationAttrsMobileApplicationRequest(
        type = ConfirmationType.MOBILE_APPLICATION,
        returnUrl = returnUrl
    )
}

internal fun PaymentOptionInfo.toRequest(paymentOption: PaymentOption): PaymentMethodDataRequest = when (this) {
    is NewCardInfo -> {
        PaymentMethodDataBankCardRequest(
            type = PaymentMethodTypeNetwork.BANK_CARD,
            card = CardRequest(number, expirationYear, expirationMonth, csc, null)
        )
    }
    is WalletInfo -> {
        PaymentMethodDataYooMoneyWalletRequest(
            type = PaymentMethodTypeNetwork.YOO_MONEY,
            instrumentType = InstrumentType.WALLET
        )
    }
    is LinkedCardInfo -> {
        PaymentMethodDataYooMoneyLinkedBankCardRequest(
            id = (paymentOption as LinkedCard).cardId,
            type = PaymentMethodTypeNetwork.YOO_MONEY,
            instrumentType = InstrumentType.LINKED_BANK_CARD,
            csc = csc
        )
    }
    is SberPay -> PaymentMethodDataSberbankRequest(type = PaymentMethodTypeNetwork.SBERBANK, phone = null)
    is SBPInfo -> PaymentMethodDataSBPRequest(type = PaymentMethodTypeNetwork.SBP)
    is SbolSmsInvoicingInfo -> {
        PaymentMethodDataSberbankRequest(
            type = PaymentMethodTypeNetwork.SBERBANK,
            phone = phone.filter(Char::isDigit)
        )
    }
    is GooglePayInfo -> {
        PaymentMethodDataGooglePayRequest(
            type = PaymentMethodTypeNetwork.GOOGLE_PAY,
            paymentMethodToken = paymentMethodToken
        )
    }
}

internal fun Amount.toRequest(): ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.Amount =
    ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.Amount(value.toString(), currency)

internal fun TokensResponse.toPaymentTokenInfo(): PaymentTokenInfo = PaymentTokenInfo(
    paymentToken = paymentToken,
    profilingInfo = ProfilingInfo(publicCardId = profilingData?.publicCardId)
)