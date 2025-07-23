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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

internal typealias BankListViewModel = RuntimeViewModel<BankList.State, BankList.Action, BankList.Effect>

internal class BankListViewModelFactory @AssistedInject constructor(
    private val reporter: Reporter,
    private val interactor: BankListInteractor,
    @Assisted
    private val bankListParams: BankListAssistedParams,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RuntimeViewModel<BankList.State, BankList.Action, BankList.Effect>(
            featureName = BANK_LIST_FEATURE,
            initial = {
                Out(BankList.State.Progress) {
                    input { showState(state) }
                    input { interactor.getSbpWidgetBanks(bankListParams.paymentId) }
                }
            },
            logic = {
                BankListAnalytics(
                    reporter,
                    BankListBusinessLogic(
                        showState = showState,
                        showEffect = showEffect,
                        source = source,
                        interactor = interactor,
                        confirmationUrl = bankListParams.confirmationUrl,
                        paymentId = bankListParams.paymentId
                    )
                )
            }
        ) as T
    }

    @AssistedFactory
    internal interface AssistedBankListVmFactory {
        fun create(bankListParams: BankListAssistedParams): BankListViewModelFactory
    }

    internal class BankListAssistedParams(
        val confirmationUrl: String,
        val paymentId: String,
    )

    companion object {
        const val BANK_LIST_FEATURE = "BankListViewModel"
    }
}
