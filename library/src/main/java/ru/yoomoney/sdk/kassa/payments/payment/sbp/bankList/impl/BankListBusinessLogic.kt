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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl

import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class BankListBusinessLogic(
    val showState: suspend (BankList.State) -> BankList.Action,
    val showEffect: suspend (BankList.Effect) -> Unit,
    val source: suspend () -> BankList.Action,
    val interactor: BankListInteractor,
    val confirmationUrl: String,
    val paymentId: String,
) : Logic<BankList.State, BankList.Action> {

    override fun invoke(
        state: BankList.State,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> {
        return when (state) {
            is BankList.State.Progress -> handleProgress(state, action)
            is BankList.State.Error -> handleError(state, action)
            is BankList.State.BankListContent -> handleBankListContent(state, action)
            is BankList.State.BankListStatusProgress -> handleBankListStatusProgress(state, action)
            is BankList.State.PaymentBankListStatusError -> handleBankListPaymentStatusError(state, action)
            is BankList.State.ActivityNotFoundError -> handleActivityNotFoundStatusError(state, action)
        }
    }

    private fun handleBankListContent(
        bankListContent: BankList.State.BankListContent,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.BackToBankList -> Out(bankListContent) {
            input(source)
            output { showEffect(BankList.Effect.CloseBankList) }
        }

        is BankList.Action.SelectBank -> handleOpenBank(bankListContent, action.url)
        is BankList.Action.BankInteractionFinished -> handleBankInteractionFinished(bankListContent)

        is BankList.Action.ActivityNotFound -> handleActivityNotFound(action.throwable, bankListContent)
        is BankList.Action.Search -> Out(
            bankListContent.copy(
                searchText = action.searchText,
                searchedBanks = interactor.searchBank(action.searchText, bankListContent.bankList)
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.CancelSearch -> Out(
            bankListContent.copy(
                searchText = "",
                searchedBanks = emptyList()
            )
        ) {
            input { showState(this.state) }
        }

        else -> Out.skip(bankListContent, source)
    }

    private fun handleBankInteractionFinished(bankListContent: BankList.State.BankListContent) =
        Out(
            BankList.State.BankListStatusProgress(bankListContent.bankList)
        ) {
            input { showState(this.state) }
            input { interactor.getPaymentStatus(paymentId) }
        }

    private fun handleProgress(
        state: BankList.State.Progress,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadBankListSuccess -> Out(
            BankList.State.BankListContent(action.bankList)
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.LoadBankListFailed -> Out(BankList.State.Error(action.throwable)) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleBankListStatusProgress(
        state: BankList.State.BankListStatusProgress,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.PaymentProcessInProgress -> Out(
            BankList.State.BankListContent(state.bankList)
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.PaymentProcessFinished -> Out(state) {
            input(source)
            output { showEffect(BankList.Effect.CloseBankListWithFinish) }
        }

        is BankList.Action.PaymentStatusError -> Out(
            BankList.State.PaymentBankListStatusError(
                action.throwable,
                state.bankList
            )
        ) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleError(
        state: BankList.State.Error,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadBankList -> Out(BankList.State.Progress) {
            input { showState(this.state) }
            input { interactor.getSbpWidgetBanks(paymentId) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleBankListPaymentStatusError(
        state: BankList.State.PaymentBankListStatusError,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadPaymentStatus, BankList.Action.BankInteractionFinished -> Out(
            BankList.State.BankListStatusProgress(state.bankList)
        ) {
            input { showState(this.state) }
            input { interactor.getSbpWidgetBanks(paymentId) }
            input { interactor.getPaymentStatus(paymentId) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleOpenBank(state: BankList.State, deeplink: String) = Out(state) {
        input(source)
        output { showEffect(BankList.Effect.OpenBank(deeplink)) }
        interactor.bankWasSelected = true
    }

    private fun handleActivityNotFound(throwable: Throwable, state: BankList.State) =
        Out(BankList.State.ActivityNotFoundError(throwable, state)) {
            input { showState(this.state) }
        }

    private fun handleActivityNotFoundStatusError(
        state: BankList.State.ActivityNotFoundError,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.BackToBankList -> Out(state.previosListState) {
            input { showState(this.state) }
        }

        is BankList.Action.BankInteractionFinished -> Out(state) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }
}
