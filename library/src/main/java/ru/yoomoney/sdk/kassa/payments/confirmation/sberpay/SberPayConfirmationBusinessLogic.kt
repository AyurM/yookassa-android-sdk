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

import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationContract.Action
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationContract.Effect
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationContract.State
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class SberPayConfirmationBusinessLogic(
    private val showState: suspend (State) -> Action,
    private val showEffect: suspend (Effect) -> Unit,
    private val source: suspend () -> Action,
    private val sberPayConfirmationInteractor: SberPayConfirmationInteractor,
    private val confirmationData: String,
    private val shopId: String,
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.LoadSberPayInfoFailed -> state.whenFailed(action)
    }

    private fun State.Loading.whenLoading(
        action: Action,
    ): Out<State, Action> = when (action) {
        is Action.FinishedPaymentStatus -> Out(this) {
            input(source)
            output { showEffect(Effect.CloseWithFinishState) }
        }

        is Action.LoadSberPayInfoSuccess -> Out(this) {
            input(source)
            output {
                showEffect(
                    Effect.ShowSberPaySdk(
                        action.sberPayApikey,
                        action.merchantLogin,
                        action.orderId,
                        action.orderNumber
                    )
                )
            }
        }

        is Action.LoadSberPayInfoFailed -> Out(State.LoadSberPayInfoFailed(action.throwable)) {
            input { showState(this.state) }
        }

        else -> Out.skip(this, source)
    }

    private fun State.LoadSberPayInfoFailed.whenFailed(
        action: Action,
    ): Out<State, Action> = when (action) {
        is Action.LoadSberPayInfo -> Out(State.Loading) {
            input { showState(this.state) }
            input { sberPayConfirmationInteractor.loadSberPayInfo(confirmationData, shopId) }
        }

        else -> Out.skip(this, source)
    }
}