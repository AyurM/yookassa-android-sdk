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

package ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl

import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract.Action
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract.Effect
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract.State
import ru.yoomoney.sdk.kassa.payments.model.UserPaymentProcess
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class SBPConfirmationBusinessLogic(
    private val showState: suspend (State) -> Action,
    private val showEffect: suspend (Effect) -> Unit,
    private val source: suspend () -> Action,
    private val paymentDetailsUseCase: SBPConfirmationUseCase,
) : Logic<State, Action> {

    override fun invoke(
        state: State,
        action: Action,
    ): Out<State, Action> = when (state) {
        is State.Loading -> state.whenLoading(action)
        is State.LoadingDataFailed -> state.whenLoadingDataFailed(action)
    }

    private fun State.Loading.whenLoading(
        action: Action,
    ): Out<State, Action> {
        return when (action) {
            is Action.GetConfirmationDetails -> action.getConfirmationDetails()
            is Action.GetConfirmationDetailsSuccess -> Out(this) {
                input { showState(this.state) }
                input { paymentDetailsUseCase.getPaymentDetails(action.paymentId, action.confirmationUrl) }
            }
            is Action.GetPaymentDetailsSuccess -> action.handleResultPaymentDetails()
            is Action.GetPaymentDetailsFailed -> action.throwable.showLoadingDataFailed()
            is Action.GetConfirmationDetailsFailed -> action.throwable.showLoadingDataFailed()
        }
    }

    private fun State.LoadingDataFailed.whenLoadingDataFailed(
        action: Action,
    ): Out<State, Action> {
        return when (action) {
            is Action.GetConfirmationDetails -> action.getConfirmationDetails()
            else -> Out.skip(this, source)
        }
    }

    private fun Action.GetConfirmationDetails.getConfirmationDetails() = Out(State.Loading) {
        input { showState(this.state) }
        input { paymentDetailsUseCase.getConfirmationDetails(confirmationData) }
    }

    private fun Throwable.showLoadingDataFailed() = Out(State.LoadingDataFailed(this)) {
        input { showState(this.state) }
    }

    private fun Action.GetPaymentDetailsSuccess.handleResultPaymentDetails(): Out<State, Action> {
        return if (userPaymentProcess == UserPaymentProcess.FINISHED) {
            Out(State.Loading) {
                output {
                    showEffect(Effect.SendFinishState)
                }
            }
        } else {
            Out(State.Loading) {
                output {
                    showEffect(Effect.ContinueSBPConfirmation(confirmationUrl, paymentId))
                }
            }
        }
    }
}
