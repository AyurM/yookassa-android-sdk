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

import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentInstrumentYooMoney
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.ConfigPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.Wallet

internal fun PaymentInstrumentYooMoney.mapToYooMoneyModel(id: Int, configPaymentOptions: List<ConfigPaymentOption>) =
    when (this) {
        is PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyWallet -> {
            this.mapToWalletModel(
                id,
                configPaymentOptions.first { it.method == paymentMethodType.value })
        }
        is PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyLinkedBankCard -> this.mapToLinkedCardModel(id)
        is PaymentInstrumentYooMoney.AbstractYooMoneyWallet -> {
            this.mapToAbstractWalletModel(
                id,
                configPaymentOptions.first { it.method == paymentMethodType.value }
            )
        }
    }

internal fun PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyWallet.mapToWalletModel(
    id: Int,
    configPaymentOption: ConfigPaymentOption,
) = Wallet(
    id = id,
    charge = charge.mapToAmountModel(),
    fee = fee?.mapToFeeModel(),
    walletId = this.id,
    balance = balance?.mapToAmountModel(),
    icon = configPaymentOption.iconUrl,
    title = configPaymentOption.title,
    savePaymentMethodAllowed = savePaymentMethod.isAllowed(),
    confirmationTypes = confirmationTypes?.map { it.mapToConformationModel() } ?: emptyList(),
    savePaymentInstrument = savePaymentInstrument
)

internal fun PaymentInstrumentYooMoney.PaymentInstrumentYooMoneyLinkedBankCard.mapToLinkedCardModel(
    id: Int,
) = LinkedCard(
    id = id,
    charge = charge.mapToAmountModel(),
    fee = fee?.mapToFeeModel(),
    icon = null,
    title = null,
    cardId = this.id,
    brand = cardType.mapToCardBrand(),
    pan = cardMask,
    name = cardName,
    isLinkedToWallet = true,
    savePaymentMethodAllowed = savePaymentMethod.isAllowed(),
    confirmationTypes = confirmationTypes?.map { it.mapToConformationModel() } ?: emptyList(),
    savePaymentInstrument = savePaymentInstrument
)

internal fun PaymentInstrumentYooMoney.AbstractYooMoneyWallet.mapToAbstractWalletModel(
    id: Int,
    configPaymentOption: ConfigPaymentOption,
) = AbstractWallet(
    id = id,
    charge = charge.mapToAmountModel(),
    fee = fee?.mapToFeeModel(),
    icon = configPaymentOption.iconUrl,
    title = configPaymentOption.title,
    savePaymentMethodAllowed = savePaymentMethod.isAllowed(),
    confirmationTypes = confirmationTypes?.map { it.mapToConformationModel() } ?: emptyList(),
    savePaymentInstrument = savePaymentInstrument
)