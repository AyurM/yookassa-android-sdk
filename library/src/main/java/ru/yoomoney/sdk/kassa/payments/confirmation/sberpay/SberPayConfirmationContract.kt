/*
 * The MIT License (MIT)
 * Copyright © 2024 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.confirmation.sberpay

internal class SberPayConfirmationContract {

    internal sealed class State {
        data object Loading : State()
        data class LoadSberPayInfoFailed(val throwable: Throwable) : State()
    }

    internal sealed class Action {

        data object LoadSberPayInfo : Action()

        data object FinishedPaymentStatus : Action()

        data class LoadSberPayInfoSuccess(
            val sberPayApikey: String,
            val merchantLogin: String,
            val orderId: String,
            val orderNumber: String
        ) : Action()

        data class LoadSberPayInfoFailed(val throwable: Throwable) : Action()
    }

    internal sealed class Effect {
        data object CloseWithFinishState : Effect()

        data class ShowSberPaySdk(
            val sberPayApikey: String,
            val merchantLogin: String,
            val orderId: String,
            val orderNumber: String
        ) : Effect()
    }
}