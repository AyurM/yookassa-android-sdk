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

import ru.yoomoney.sdk.kassa.payments.api.model.config.SavePaymentMethodOptionTextsResponse
import ru.yoomoney.sdk.kassa.payments.model.SavePaymentMethodOptionTexts

internal fun SavePaymentMethodOptionTextsResponse.mapToSavePaymentMethodOptionTexts() = SavePaymentMethodOptionTexts(
    switchRecurrentOnBindOnTitle = switchRecurrentOnBindOnTitle,
    switchRecurrentOnBindOnSubtitle = switchRecurrentOnBindOnSubtitle,
    switchRecurrentOnBindOffTitle = switchRecurrentOnBindOffTitle,
    switchRecurrentOnBindOffSubtitle = switchRecurrentOnBindOffSubtitle,
    switchRecurrentOffBindOnTitle = switchRecurrentOffBindOnTitle,
    switchRecurrentOffBindOnSubtitle = switchRecurrentOffBindOnSubtitle,
    messageRecurrentOnBindOnTitle = messageRecurrentOnBindOnTitle,
    messageRecurrentOnBindOnSubtitle = messageRecurrentOnBindOnSubtitle,
    messageRecurrentOnBindOffTitle = messageRecurrentOnBindOffTitle,
    messageRecurrentOnBindOffSubtitle = messageRecurrentOnBindOffSubtitle,
    messageRecurrentOffBindOnTitle = messageRecurrentOffBindOnTitle,
    messageRecurrentOffBindOnSubtitle = messageRecurrentOffBindOnSubtitle,
    screenRecurrentOnBindOnTitle = screenRecurrentOnBindOnTitle,
    screenRecurrentOnBindOnText = screenRecurrentOnBindOnText,
    screenRecurrentOnBindOffTitle = screenRecurrentOnBindOffTitle,
    screenRecurrentOnBindOffText = screenRecurrentOnBindOffText,
    screenRecurrentOffBindOnTitle = screenRecurrentOffBindOnTitle,
    screenRecurrentOffBindOnText = screenRecurrentOffBindOnText,
    screenRecurrentOnSberpayTitle = screenRecurrentOnSberpayTitle,
    screenRecurrentOnSberpayText = screenRecurrentOnSberpayText,
    messageRecurrentOnSBPTitle = messageRecurrentOnSBPTitle,
    switchRecurrentOnSBPTitle = switchRecurrentOnSBPTitle,
    messageRecurrentOnSberPayTitle = messageRecurrentOnSberPayTitle,
    switchRecurrentOnSberPayTitle = switchRecurrentOnSberPayTitle
)