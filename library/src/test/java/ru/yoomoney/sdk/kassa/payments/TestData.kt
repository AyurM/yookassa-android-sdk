/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

@file:JvmName("TestData")

package ru.yoomoney.sdk.kassa.payments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.RUB
import ru.yoomoney.sdk.kassa.payments.model.AbstractWallet
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.Config
import ru.yoomoney.sdk.kassa.payments.model.ConfirmationType
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SavePaymentMethodOptionTexts
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import java.math.BigDecimal
import java.util.Arrays

const val logoUrl = "https://static.yoomoney.ru/mobile-app-content-front/msdk/payment-options/v1/iokassa-light-eng.png"

fun <T> on(arg: T): OngoingStubbing<T> = `when`(arg)

internal fun createNewCardPaymentOption(
    id: Int,
    savePaymentMethodAllowed: Boolean = true,
    createBinding: Boolean = false,
): PaymentOption =
    BankCardPaymentOption(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = listOf(ConfirmationType.EXTERNAL),
        paymentInstruments = emptyList(),
        savePaymentInstrument = createBinding,
        icon = null,
        title = null
    )

internal fun createBankCardPaymentOption(
    id: Int,
    instruments: List<PaymentInstrumentBankCard>,
    charge: Amount = Amount(BigDecimal.TEN, RUB),
    fee: Fee = Fee(
        Amount(BigDecimal.ONE, RUB),
        Amount(BigDecimal("0.5"), RUB)
    ),
    savePaymentMethodAllowed: Boolean = true,
    savePaymentInstrument: Boolean = false,
    confirmationTypes: List<ConfirmationType> = listOf(ConfirmationType.EXTERNAL),
): PaymentOption =
    BankCardPaymentOption(
        id = id,
        charge = charge,
        fee = fee,
        savePaymentMethodAllowed = savePaymentMethodAllowed,
        confirmationTypes = confirmationTypes,
        icon = null,
        title = null,
        paymentInstruments = instruments,
        savePaymentInstrument = savePaymentInstrument
    )

internal fun createWalletPaymentOption(id: Int): PaymentOption =
    Wallet(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        walletId = "12345654321",
        balance = Amount(BigDecimal.TEN, RUB),
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        icon = null,
        title = null,
        savePaymentInstrument = false
    )

internal fun createAbstractWalletPaymentOption(id: Int): PaymentOption =
    AbstractWallet(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        icon = null,
        title = null,
        savePaymentInstrument = false
    )

internal fun createSbolSmsInvoicingPaymentOption(id: Int, isSberPayAllowed: Boolean): PaymentOption =
    SberBank(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        savePaymentMethodAllowed = false,
        confirmationTypes = if (isSberPayAllowed) {
            listOf(ConfirmationType.EXTERNAL, ConfirmationType.REDIRECT, ConfirmationType.MOBILE_APPLICATION)
        } else {
            listOf(ConfirmationType.EXTERNAL, ConfirmationType.REDIRECT)
        },
        icon = null,
        title = null,
        savePaymentInstrument = false
    )

internal fun createLinkedCardPaymentOption(id: Int): PaymentOption =
    LinkedCard(
        id = id,
        charge = Amount(BigDecimal.TEN, RUB),
        fee = Fee(
            Amount(BigDecimal.ONE, RUB),
            Amount(BigDecimal("0.5"), RUB)
        ),
        cardId = "12345654321",
        brand = CardBrand.MASTER_CARD,
        pan = "1234567887654321",
        savePaymentMethodAllowed = true,
        confirmationTypes = listOf(ConfirmationType.REDIRECT),
        icon = null,
        title = null,
        savePaymentInstrument = false
    )

internal fun equalToDrawable(drawable: Drawable?): Matcher<Drawable?> = object : BaseMatcher<Drawable?>() {
    override fun describeTo(description: Description?) {
        description?.appendText("drawables not matches ")
    }

    override fun matches(item: Any?): Boolean {
        if (drawable === item) return true
        return (item as? Drawable)?.pixelsEqualTo(drawable) == true
    }
}

internal fun createPaymentInstrumentBankCard(cscRequired: Boolean = false) = PaymentInstrumentBankCard(
    paymentInstrumentId = "paymentInstrumentId",
    last4 = "4321",
    first6 = "123456",
    cscRequired = cscRequired,
    cardType = CardBrand.MASTER_CARD
)

internal val savePaymentMethodOptionTexts = SavePaymentMethodOptionTexts(
    switchRecurrentOnBindOnTitle = "Запомнить карту и разрешить автосписания",
    switchRecurrentOnBindOnSubtitle = "<a href=''>Как это работает</>",
    switchRecurrentOnBindOffTitle = "Разрешить автосписания",
    switchRecurrentOnBindOffSubtitle = "<a href=''>Как это работает</>",
    switchRecurrentOffBindOnTitle = "Запомнить карту",
    switchRecurrentOffBindOnSubtitle = "<a href=''>Как это работает</>",
    messageRecurrentOnBindOnTitle = "Заплатив здесь, вы разрешите автосписания и запомнить карту",
    messageRecurrentOnBindOnSubtitle = "<a href=''>Как это работает</>",
    messageRecurrentOnBindOffTitle = "Заплатив здесь, вы разрешаете автосписания",
    messageRecurrentOnBindOffSubtitle = "<a href=''>Как это работает</>",
    messageRecurrentOffBindOnTitle = "Заплатив здесь, вы разрешаете запомнить карту",
    messageRecurrentOffBindOnSubtitle = "<a href=''>Как это работает</>",
    messageRecurrentOnSBPTitle = "Вы привязываете счёт по правилам СБП и разрешаете автосписания",
    messageRecurrentOnSberPayTitle = "Заплатив здесь, вы разрешаете автосписания",
    switchRecurrentOnSBPTitle = "Разрешить автосписания",
    switchRecurrentOnSberPayTitle = "Разрешить автосписания",
    screenRecurrentOnBindOnTitle = "Автосписания <br>и сохранение платёжных данных",
    screenRecurrentOnBindOnText = "Если вы это разрешили, мы сохраним для этого магазина и его партнёров данные вашей банковской карты &#8212; номер, имя владельца, срок действия (всё, кроме кода CVC). В следующий раз не нужно будет их вводить, чтобы заплатить в этом магазине. <br> <br>Кроме того, мы привяжем карту к магазину. После этого магазин сможет присылать запросы на автоматические списания денег &#8212; тогда платёж выполняется без дополнительного подтверждения с вашей стороны. <br> <br>Автосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отключить их и отвязать карту можно в любой момент &#8212; через службу поддержки магазина.",
    screenRecurrentOnBindOffTitle = "Как работают автоматические списания",
    screenRecurrentOnBindOffText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту к магазину. После этого магазин сможет присылать запросы на автоматические списания денег &#8212; тогда платёж выполняется без дополнительного подтверждения с вашей стороны. <br> <br>Автосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отключить их и отвязать карту можно в любой момент &#8212; через службу поддержки магазина.",
    screenRecurrentOffBindOnTitle = "Сохранение платёжных данных",
    screenRecurrentOffBindOnText = "Если вы это разрешили, мы сохраним для этого магазина и его партнёров данные вашей банковской карты &#8212; номер, имя владельца и срок действия (всё, кроме кода CVC). В следующий раз не нужно будет вводить их, чтобы заплатить в этом магазине. <br> <br>Удалить данные карты можно в процессе оплаты (нажмите на три точки напротив карты и выберите «Удалить карту») или через службу поддержки.",
    screenRecurrentOnSberpayTitle = "Как работают автоматические списания",
    screenRecurrentOnSberpayText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту к магазину. После этого магазин сможет присылать запросы на автоматические списания денег &#8212; тогда платёж выполняется без дополнительного подтверждения с вашей стороны. <br> <br>Автосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отключить их и отвязать карту можно в любой момент &#8212; через службу поддержки магазина."
)

internal val config = Config(
    yooMoneyLogoUrlLight = "todo",
    yooMoneyLogoUrlDark = "todo",
    paymentMethods = emptyList(),
    savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
    userAgreementUrl = "Заплатив здесь, вы принимаете <a href='https://yoomoney.ru/page?id=526623'>условия сервиса</>",
    googlePayGateway = "yoomoney",
    yooMoneyApiEndpoint = "https://sdk.yookassa.ru/api/frontend/v3",
    yooMoneyPaymentAuthorizationApiEndpoint = "https://yoomoney.ru/api/wallet-auth/v1",
    yooMoneyAuthApiEndpoint = null,
    agentSchemeProviderAgreement = null,
)

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun <T : Drawable, T1 : Drawable> T?.pixelsEqualTo(t: T1?) =
    this === null && t === null || this !== null && t !== null && this.toBitmap().pixelsEqualTo(t.toBitmap(), true)

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.pixelsEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
    if (width == other.width && height == other.height) {
        val res = Arrays.equals(toPixels(), other.toPixels())
        if (shouldRecycle) {
            doRecycle().also { otherBitmap.doRecycle() }
        }
        res
    } else false
} ?: kotlin.run { false }

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun <T : Drawable> T.toBitmap(): Bitmap {
    if (this is BitmapDrawable) return bitmap

    val drawable: Drawable = this
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.doRecycle() {
    if (!isRecycled) recycle()
}

// from https://gist.github.com/XinyueZ/3cca89416a1e443f914ed37f80ed59f2
private fun Bitmap.toPixels() = IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }