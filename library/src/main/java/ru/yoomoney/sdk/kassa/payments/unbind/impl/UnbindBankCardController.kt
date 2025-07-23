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

import androidx.activity.compose.BackHandler
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import ru.yoomoney.sdk.guiCompose.views.notice.rememberNoticeService
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardContract
import ru.yoomoney.sdk.kassa.payments.unbind.di.UnbindCardModule
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardScreen
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardScreenUiState
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardViewModel
import ru.yoomoney.sdk.kassa.payments.unbind.ui.mapToUiState
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.marchcompose.extensions.observe
import ru.yoomoney.sdk.marchcompose.extensions.observeAsUiState

@Composable
internal fun UnbindCardScreenController(
    linkedCard: LinkedCard?,
    instrumentCard: PaymentInstrumentBankCard?,
    viewModelFactory: ViewModelProvider.Factory,
    onUnbindSuccess: (last4: String) -> Unit,
    onCloseScreen: () -> Unit,
) {
    val viewModel = viewModel<UnbindCardViewModel>(UnbindCardModule.UNBIND_CARD) { viewModelFactory }
    LaunchedEffect(Unit) {
        viewModel.handleAction(UnbindCardContract.Action.StartDisplayData(linkedCard, instrumentCard))
    }
    val showProgress = remember {
        mutableStateOf(false)
    }
    val onClickUnbind: (String) -> Unit = { cardId ->
        viewModel.handleAction(
            UnbindCardContract.Action.StartUnbinding(
                cardId
            )
        )
    }
    val context = LocalContext.current
    val hostState = remember { SnackbarHostState() }
    val noticeService = rememberNoticeService(hostState)

    viewModel.effects.observe { effect ->
        when (effect) {
            is UnbindCardContract.Effect.UnbindComplete -> onUnbindSuccess(effect.instrumentBankCard.last4)
            is UnbindCardContract.Effect.UnbindFailed -> {
                showProgress.value = false
                noticeService.showAlertNotice(
                    context.getString(
                        R.string.ym_unbinding_card_failed,
                        effect.instrumentBankCard.last4
                    )
                )
            }
        }
    }
    val state =
        viewModel.states.observeAsUiState(
            initial = UnbindCardScreenUiState.Initial,
            mapToUiState = { it.mapToUiState() }).value
    UnbindCardScreen(onCloseScreen, onClickUnbind, state, hostState, noticeService)
    BackHandler {
        onCloseScreen()
    }
}