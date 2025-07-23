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

package ru.yoomoney.sdk.kassa.payments.unbind.impl

import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardContract
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class UnbindBusinessLogic(
    private val showState: suspend (UnbindCardContract.State) -> UnbindCardContract.Action,
    private val showEffect: suspend (UnbindCardContract.Effect) -> Unit,
    private val source: suspend () -> UnbindCardContract.Action,
    private val unbindCardUseCase: UnbindCardUseCase
) : Logic<UnbindCardContract.State, UnbindCardContract.Action> {

    override fun invoke(
        state: UnbindCardContract.State,
        action: UnbindCardContract.Action
    ): Out<UnbindCardContract.State, UnbindCardContract.Action> = when (state) {
        is UnbindCardContract.State.Initial -> state.whenInitialization(action)
        is UnbindCardContract.State.ContentLinkedBankCard -> state.whenUnbinding(action)
        is UnbindCardContract.State.LoadingLinkedCardUnbinding -> state.whenLoadingUnbinding(action)
        else -> Out.skip(state, source)
    }

    private fun UnbindCardContract.State.Initial.whenInitialization(
        action: UnbindCardContract.Action
    ): Out<UnbindCardContract.State, UnbindCardContract.Action> {
        return when (action) {
            is UnbindCardContract.Action.StartDisplayData -> whenStartDisplayData(action)
            else -> Out.skip(this, source)
        }
    }

    private fun UnbindCardContract.State.Initial.whenStartDisplayData(
        action: UnbindCardContract.Action.StartDisplayData
    ): Out<UnbindCardContract.State, UnbindCardContract.Action> = when {
        action.linkedCard != null -> {
            Out(UnbindCardContract.State.ContentLinkedWallet(action.linkedCard)) {
                input { showState(this.state) }
            }
        }
        action.instrumentBankCard != null -> {
            Out(UnbindCardContract.State.ContentLinkedBankCard(action.instrumentBankCard)) {
                input { showState(this.state) }
            }
        }
        else -> Out.skip(this, source)
    }

    private fun UnbindCardContract.State.ContentLinkedBankCard.whenUnbinding(
        action: UnbindCardContract.Action
    ): Out<UnbindCardContract.State, UnbindCardContract.Action> {
        return when (action) {
            is UnbindCardContract.Action.StartUnbinding -> Out(UnbindCardContract.State.LoadingLinkedCardUnbinding(this.instrumentBankCard)) {
                input { showState(this.state) }
                input { unbindCardUseCase.unbindCard(state.instrumentBankCard.paymentInstrumentId) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun UnbindCardContract.State.LoadingLinkedCardUnbinding.whenLoadingUnbinding(
        action: UnbindCardContract.Action
    ): Out<UnbindCardContract.State, UnbindCardContract.Action> {
        return when (action) {
            is UnbindCardContract.Action.StartUnbinding -> Out(UnbindCardContract.State.LoadingLinkedCardUnbinding(this.instrumentBankCard)) {
                input { showState(this.state) }
                input { unbindCardUseCase.unbindCard(state.instrumentBankCard.paymentInstrumentId) }
            }
            is UnbindCardContract.Action.UnbindSuccess -> Out(this) {
                output { showEffect(UnbindCardContract.Effect.UnbindComplete(state.instrumentBankCard)) }
                input(source)
            }
            is UnbindCardContract.Action.UnbindFailed -> Out(this) {
                output { showEffect(UnbindCardContract.Effect.UnbindFailed(state.instrumentBankCard)) }
                input(source)
            }
            else -> Out.skip(this, source)
        }
    }
}
