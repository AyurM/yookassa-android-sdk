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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui

import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain

internal sealed interface BankListUiState {
    data object Progress : BankListUiState

    data class BankListStatusProgress(
        val bankList: List<SbpWidgetBankDomain>,
    ) : BankListUiState

    data class Error(
        val throwable: Throwable,
    ) : BankListUiState

    data class BankListContent(
        val bankList: List<SbpWidgetBankDomain>,
        val searchText: String = "",
        val searchedBanks: List<SbpWidgetBankDomain> = emptyList(),
    ) : BankListUiState

    data class PaymentBankListStatusError(
        val throwable: Throwable,
        val bankList: List<SbpWidgetBankDomain>,
    ) : BankListUiState

    data class ActivityNotFoundError(val throwable: Throwable, val previosListState: BankList.State) : BankListUiState
}

internal fun BankList.State.mapToUiState(): BankListUiState = when (this) {
    is BankList.State.Progress -> BankListUiState.Progress
    is BankList.State.ActivityNotFoundError -> BankListUiState.ActivityNotFoundError(throwable, previosListState)
    is BankList.State.Error -> BankListUiState.Error(throwable)
    is BankList.State.BankListContent -> BankListUiState.BankListContent(
        bankList,
        searchText,
        searchedBanks
    )

    is BankList.State.BankListStatusProgress -> BankListUiState.BankListStatusProgress(bankList)

    is BankList.State.PaymentBankListStatusError -> BankListUiState.PaymentBankListStatusError(
        throwable,
        bankList,
    )
}