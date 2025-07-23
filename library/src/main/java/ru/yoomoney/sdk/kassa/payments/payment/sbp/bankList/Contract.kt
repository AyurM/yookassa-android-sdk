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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList

import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain

internal object BankList {

    sealed class State {

        data object Progress : State()

        data class BankListStatusProgress(
            val bankList: List<SbpWidgetBankDomain>,
        ) : State()

        data class Error(
            val throwable: Throwable,
        ) : State()

        data class BankListContent(
            val bankList: List<SbpWidgetBankDomain>,
            val searchText: String = "",
            val searchedBanks: List<SbpWidgetBankDomain> = emptyList(),
        ) : State()

        data class PaymentBankListStatusError(
            val throwable: Throwable,
            val bankList: List<SbpWidgetBankDomain>,
        ) : State()

        data class ActivityNotFoundError(val throwable: Throwable, val previosListState: State) : State()
    }

    sealed class Action {

        data object LoadBankList : Action()

        data object BackToBankList : Action()

        data class LoadBankListFailed(
            val throwable: Throwable,
        ) : Action()

        data class LoadBankListSuccess(
            val bankList: List<SbpWidgetBankDomain>,
        ) : Action()

        data class SelectBank(
            val url: String,
        ) : Action()

        data object BankInteractionFinished : Action()

        data object PaymentProcessFinished : Action()

        data object PaymentProcessInProgress : Action()

        data class PaymentStatusError(val throwable: Throwable) : Action()

        data object LoadPaymentStatus : Action()

        data class ActivityNotFound(val throwable: Throwable) : Action()

        data class Search(val searchText: String) : Action()

        data object CancelSearch : Action()
    }

    sealed class Effect {

        data class OpenBank(
            val url: String,
        ) : Effect()

        data object CloseBankList : Effect()

        data object CloseBankListWithFinish : Effect()
    }
}
