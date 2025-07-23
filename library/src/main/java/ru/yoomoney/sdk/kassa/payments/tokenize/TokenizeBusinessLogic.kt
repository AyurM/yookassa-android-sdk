/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.tokenize

import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeContract.State
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeContract.Action
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeContract.Effect

internal class TokenizeBusinessLogic(
    val showState: suspend (State) -> Action,
    val showEffect: suspend (Effect) -> Unit,
    val source: suspend () -> Action,
    private val tokenizeUseCase: TokenizeUseCase,
) : Logic<State, Action> {

    override fun invoke(state: State, action: Action): Out<State, Action> {
        return when (state) {
            is State.Start -> state.whenState(action)
            is State.Tokenize -> state.whenState(action)
            is State.TokenizeError -> state.whenState(action)
        }
    }

    private fun State.Start.whenState(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Tokenize -> Out(State.Tokenize(action.tokenizeInputModel)) {
                input { showState(this.state) }
                input { tokenizeUseCase.tokenize(this.state.tokenizeInputModel) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.Tokenize.whenState(action: Action): Out<State, Action> {
        return when (action) {
            is Action.PaymentAuthRequired -> Out(this) {
                input(source)
                output {
                    showEffect(
                        Effect.PaymentAuthRequired(
                            action.charge,
                            state.tokenizeInputModel.allowWalletLinking
                        )
                    )
                }
            }
            is Action.PaymentAuthSuccess -> Out(this) {
                input(source)
                input { tokenizeUseCase.tokenize(this.state.tokenizeInputModel) }
            }
            is Action.PaymentAuthCancel -> Out(this) {
                input { showState(this.state) }
                output { showEffect(Effect.CancelTokenize) }
            }
            is Action.TokenizeSuccess -> Out(this) {
                val effect = Effect.TokenizeComplete(action.content, state.tokenizeInputModel.allowWalletLinking)
                output { showEffect(effect) }
                input(source)
            }
            is Action.TokenizeFailed -> Out(State.TokenizeError(this.tokenizeInputModel, action.error)) {
                input { showState(this.state) }
            }
            else -> Out.skip(this, source)
        }
    }

    private fun State.TokenizeError.whenState(action: Action): Out<State, Action> {
        return when (action) {
            is Action.Tokenize -> Out(State.Tokenize(action.tokenizeInputModel)) {
                input { showState(this.state) }
                input { tokenizeUseCase.tokenize(this.state.tokenizeInputModel) }
            }
            else -> Out.skip(this, source)
        }
    }
}