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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListInteractor
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModel
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModelFactory
import ru.yoomoney.sdk.kassa.payments.utils.compose.OnLifecycleEvent
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.marchcompose.extensions.observe
import ru.yoomoney.sdk.marchcompose.extensions.observeAsUiState

@Composable
internal fun BankListController(
    bankListParams: BankListViewModelFactory.BankListAssistedParams,
    errorFormatter: ErrorFormatter,
    viewModelFactory: BankListViewModelFactory.AssistedBankListVmFactory,
    bankListInteractor: BankListInteractor,
    closeBankList: () -> Unit,
    closeBankListWithFinish: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<BankListViewModel>(key = BankListViewModelFactory.BANK_LIST_FEATURE) {
        viewModelFactory.create(bankListParams)
    }
    viewModel.effects.observe(onElement = { effect ->
        when (effect) {
            is BankList.Effect.OpenBank -> openBank(context, effect.url, viewModel)
            is BankList.Effect.CloseBankList -> closeBankList()
            is BankList.Effect.CloseBankListWithFinish -> closeBankListWithFinish()
        }
    })

    BankListScreen(
        state = viewModel.states.observeAsUiState(
            initial = BankListUiState.Progress,
            mapToUiState = { it.mapToUiState() }).value,
        errorFormatter = errorFormatter,
        viewModel = viewModel,
    )

    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && bankListInteractor.bankWasSelected) {
            viewModel.handleAction(BankList.Action.BankInteractionFinished)
        }
    }
}

private fun openBank(context: Context, deeplink: String, viewModel: BankListViewModel) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(deeplink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        viewModel.handleAction(BankList.Action.ActivityNotFound(exception))
    }
}