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

package ru.yoomoney.sdk.kassa.payments.confirmation.sbp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.Channel
import ru.yoomoney.sdk.guiCompose.views.notice.Notice
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationVMFactory
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentDetailsModule
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.marchcompose.extensions.observe
import ru.yoomoney.sdk.marchcompose.extensions.observeAsUiState

internal typealias ConfirmationViewModel = RuntimeViewModel<SBPConfirmationContract.State, SBPConfirmationContract.Action, SBPConfirmationContract.Effect>

@Composable
internal fun SBPConfirmationController(
    navigateToBankList: (String, String) -> Unit,
    navigateToSuccessful: () -> Unit,
    confirmationData: String,
    errorFormatter: ErrorFormatter,
    viewModelFactory: SBPConfirmationVMFactory.AssistedSBPConfirmationVMFactory,
) {
    val context = LocalContext.current
    val notices = remember { Channel<Notice>() }
    val viewModel = viewModel<ConfirmationViewModel>(PaymentDetailsModule.PAYMENT_DETAILS) {
        viewModelFactory.create(confirmationData)
    }

    viewModel.effects.observe { effect ->
        when (effect) {
            is SBPConfirmationContract.Effect.ContinueSBPConfirmation -> {
                navigateToBankList(effect.confirmationUrl, effect.paymentId)
            }

            is SBPConfirmationContract.Effect.SendFinishState -> {
                navigateToSuccessful()
            }
        }
    }

    SBPConfirmationScreen(
        state = viewModel.states.observeAsUiState(SBPConfirmationState.Loading) {
            it.mapToUiState(
                errorFormatter = errorFormatter,
                context = context,
                onReload = {
                    viewModel.handleAction(SBPConfirmationContract.Action.GetConfirmationDetails(confirmationData))
                }
            )
        }.value,
        notices = notices
    )
}