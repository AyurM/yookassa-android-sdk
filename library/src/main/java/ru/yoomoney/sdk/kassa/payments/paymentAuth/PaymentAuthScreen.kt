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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.guiCompose.views.buttons.FlatButtonView
import ru.yoomoney.sdk.guiCompose.views.form.ConfirmCodeInputView
import ru.yoomoney.sdk.guiCompose.views.topbar.TopBarDefault
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.toHint
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.paymentAuth.di.PaymentAuthModule
import ru.yoomoney.sdk.kassa.payments.ui.CustomDimens
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.LoadingStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.marchcompose.extensions.observe
import ru.yoomoney.sdk.marchcompose.extensions.observeAsUiState
import java.util.concurrent.TimeUnit
import ru.yoomoney.sdk.gui.gui.R as GuiResources

@Composable
internal fun PaymentAuthScreen(
    errorFormatter: ErrorFormatter,
    linkWalletToAp: Boolean,
    amount: Amount,
    viewModelFactory: ViewModelProvider.Factory,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel = viewModel<PaymentAuthViewModel>(PaymentAuthModule.PAYMENT_AUTH) { viewModelFactory }
    val keyboardController = LocalSoftwareKeyboardController.current
    viewModel.exceptions.observe { _ -> viewModel.handleAction(PaymentAuth.Action.Start(linkWalletToAp, amount)) }
    val onEnteredCode: (code: String) -> Unit = {
        viewModel.handleAction(
            PaymentAuth.Action.ProcessAuthRequired(
                it,
                true
            )
        )
    }
    val onClickRetry = {
        viewModel.handleAction(PaymentAuth.Action.Start(linkWalletToAp, amount))
    }
    val state =
        viewModel.states.observeAsUiState(
            initial = PaymentAuthUiState.Loading,
            mapToUiState = { it.mapToUiState() }).value

    viewModel.effects.observe { effect ->
        when (effect) {
            is PaymentAuth.Effect.ShowSuccess -> {
                keyboardController?.hide()
                onSuccess()
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.handleAction(PaymentAuth.Action.Start(linkWalletToAp, amount))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .roundBottomSheetCorners()
    ) {
        when (state) {
            is PaymentAuthUiState.Loading -> {
                Loading()
            }

            is PaymentAuthUiState.StartError -> ShowError(errorText = errorFormatter.format(state.error).toString()) {
                viewModel.handleAction(PaymentAuth.Action.Start(linkWalletToAp, amount))
            }

            is PaymentAuthUiState.InputCode -> ShowInputCode(state, onEnteredCode, onCancel, onClickRetry)
            is PaymentAuthUiState.InputCodeProcess -> ShowInputCodeProcess(state, onEnteredCode, onCancel, onClickRetry)
            is PaymentAuthUiState.InputCodeVerifyExceeded -> ShowInputCodeVerifyExceeded(
                state,
                onEnteredCode,
                onCancel,
                onClickRetry
            )

            is PaymentAuthUiState.WrongInputCode -> WrongAnswerScreen(
                state.data,
                state.attemptsCount,
                state.attemptsLeft,
                onCancel,
                onClickRetry,
                onEnteredCode
            )

            is PaymentAuthUiState.ProcessError -> ShowError(errorText = errorFormatter.format(state.error).toString()) {
                viewModel.handleAction(PaymentAuth.Action.StartSuccess(state.data))
            }
        }
    }
    BackHandler {
        onCancel()
    }
}

@Composable
private fun rememberCountdownTimerState(
    initialMillis: Long,
    step: Long = 1000,
): MutableState<Long> {
    val timeLeft = remember { mutableStateOf(initialMillis) }
    LaunchedEffect(initialMillis, step) {
        val startTime = withFrameMillis { it }
        while (isActive && timeLeft.value > 0) {
            val duration = withFrameMillis { time ->
                (time - startTime).coerceAtLeast(0)
            }
            timeLeft.value = (initialMillis - duration).coerceAtLeast(0)
            delay(step.coerceAtMost(timeLeft.value))
        }
    }
    return timeLeft
}

@Composable
private fun WrongAnswerScreen(
    data: AuthTypeState.SMS,
    attemptsCount: Int?,
    attemptsLeft: Int?,
    onCancel: () -> Unit,
    onClickRetry: () -> Unit,
    onEnteredCode: (String) -> Unit,
) {
    ShowInputCode(
        data = data,
        passphrase = null,
        onEnteredCode = onEnteredCode,
        onCancel = onCancel,
        confirmCodeError = formatErrorText(LocalContext.current, attemptsCount, attemptsLeft),
        showProgress = false,
        onClickRetry = onClickRetry
    )
}

private fun formatErrorText(
    context: Context,
    attemptsCount: Int?,
    attemptsLeft: Int?,
): String {
    return if (attemptsCount != null && attemptsLeft != null) {
        when (attemptsLeft) {
            attemptsCount - 1 -> context.getString(R.string.ym_payment_auth_wrong_code_try_again)
            0 -> context.getString(R.string.ym_payment_auth_no_attempts)
            else -> context.getString(R.string.ym_payment_auth_wrong_code_with_attempts, attemptsLeft)
        }
    } else {
        context.getString(R.string.ym_payment_auth_wrong_code)
    }
}

private fun formatRetryTimeLeftText(context: Context, millis: Long): String {
    val secondsNow = millis / 1000
    val minutes = (secondsNow / 60).toTimeString()
    val seconds = (secondsNow % 60).toTimeString()
    return context.getString(R.string.ym_confirm_retry_timer_text, "$minutes:$seconds")
}

@Composable
private fun Loading() {
        LoadingStateScreen(
            Modifier
                .defaultMinSize(minHeight = CustomDimens.bottomDialogMinHeight)
                .fillMaxWidth())
}

@Composable
private fun ShowError(errorText: String, action: () -> Unit) {
    ErrorStateScreen(
        modifier = Modifier
            .fillMaxWidth()
            .height(ru.yoomoney.sdk.kassa.payments.ui.compose.CustomDimens.bottomDialogMaxHeight),
        title = errorText,
        action = stringResource(id = R.string.ym_retry),
        onClick = action
    )
}

@Composable
private fun ShowInputCode(
    inputCode: PaymentAuthUiState.InputCode,
    onEnteredCode: (String) -> Unit,
    onCancel: () -> Unit,
    onClickRetry: () -> Unit,
) {
    ShowInputCode(
        inputCode.data,
        null,
        onEnteredCode,
        onCancel,
        "",
        false,
        onClickRetry
    )
}

@Composable
private fun ShowInputCodeProcess(
    inputCodeProcess: PaymentAuthUiState.InputCodeProcess,
    onEnteredCode: (String) -> Unit,
    onCancel: () -> Unit,
    onClickRetry: () -> Unit,
) {
    ShowInputCode(
        inputCodeProcess.data,
        inputCodeProcess.passphrase,
        onEnteredCode,
        onCancel,
        "",
        showProgress = true,
        onClickRetry
    )
}

@Composable
private fun ShowInputCodeVerifyExceeded(
    inputCodeProcess: PaymentAuthUiState.InputCodeVerifyExceeded, onEnteredCode: (String) -> Unit,
    onCancel: () -> Unit,
    onClickRetry: () -> Unit,
) {
    ShowInputCode(
        inputCodeProcess.data,
        inputCodeProcess.passphrase,
        onEnteredCode,
        onCancel,
        stringResource(id = R.string.ym_payment_auth_no_attempts),
        showProgress = false,
        onClickRetry,
        confirmCodeInputIsEnabled = false
    )
}

@Composable
private fun ShowInputCode(
    data: AuthTypeState.SMS,
    passphrase: String?,
    onEnteredCode: (String) -> Unit,
    onCancel: () -> Unit,
    confirmCodeError: String,
    showProgress: Boolean,
    onClickRetry: () -> Unit,
    confirmCodeInputIsEnabled: Boolean = true,
) {
    var confirmCodeText by remember { mutableStateOf(passphrase.orEmpty()) }
    var codeConfirmError by remember {
        mutableStateOf(confirmCodeError)
    }
    val timeLeft by rememberCountdownTimerState(TimeUnit.SECONDS.toMillis(data.nextSessionTimeLeft.toLong()))
    var retryTimeLeft by remember {
        mutableStateOf("")
    }
    var retryIsEnable by remember {
        mutableStateOf(false)
    }
    if (timeLeft != 0L) {
        retryIsEnable = false
        retryTimeLeft = formatRetryTimeLeftText(LocalContext.current, timeLeft)
    } else {
        retryIsEnable = true
    }

    Column(
        Modifier
            .defaultMinSize(minHeight = CustomDimens.bottomDialogMinHeight)
            .wrapContentHeight()
            .background(YooTheme.colors.background.default)
    ) {
        TopBarDefault(onNavigationClick = onCancel)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(YooTheme.dimens.spaceM),
            text = data.type.toHint(LocalContext.current).toString(),
            style = YooTheme.typography.title1,
            textAlign = TextAlign.Center
        )
        ConfirmCodeInputView(
            text = confirmCodeText, modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = YooTheme.dimens.spaceM,
                    end = YooTheme.dimens.spaceM,
                    top = YooTheme.dimens.space2XL,
                    bottom = YooTheme.dimens.space2XL
                ), length = data.codeLength, onTextChange = { value ->
                codeConfirmError = ""
                confirmCodeText = value
                if (value.length == data.codeLength) {
                    onEnteredCode(value)
                }
            },
            enabled = confirmCodeInputIsEnabled
        )
        ErrorText(codeConfirmError)
        RetryContent(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            showProgress,
            retryTimeLeft,
            retryIsEnable,
            onClickRetry = {
                onClickRetry()
                codeConfirmError = ""
            })
    }
}

@Composable
private fun RetryContent(
    modifier: Modifier,
    showProgress: Boolean,
    retryTimeLeft: String,
    retryIsEnable: Boolean,
    onClickRetry: () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (showProgress) {
            LoadingStateScreen()
        } else {
            if (retryIsEnable) {
                FlatButtonView(text = stringResource(id = R.string.ym_payment_auth_retry_text), modifier = modifier
                    .padding(
                        start = YooTheme.dimens.spaceM,
                        end = YooTheme.dimens.spaceM,
                        bottom = YooTheme.dimens.spaceM
                    ), onClick = {
                    onClickRetry()
                })
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(YooTheme.dimens.spaceM),
                    text = retryTimeLeft,
                    style = YooTheme.typography.body,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ErrorText(codeConfirmError: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(YooTheme.dimens.spaceM),
        text = codeConfirmError,
        style = YooTheme.typography.caption2,
        textAlign = TextAlign.Center,
        color = colorResource(id = GuiResources.color.color_type_alert)
    )
}

@Composable
@Preview(showBackground = true)
private fun LoadingPreview() {
    MoneyPaymentComposeContent(isDarkMode = false) {
        Loading()
    }
}

@Composable
@Preview(showBackground = true)
private fun ContentPreview() {
    MoneyPaymentComposeContent(isDarkMode = false) {
        ShowInputCode(
            AuthTypeState.SMS(2000, 5, 3, 2),
            "123",
            {},
            {},
            "test error",
            showProgress = false,
            {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ContentPreviewProgress() {
    MoneyPaymentComposeContent(isDarkMode = false) {
        ShowInputCode(
            AuthTypeState.SMS(2000, 5, 3, 2),
            "123",
            {},
            {},
            "",
            showProgress = true,
            {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ContentPreviewDark() {
    MoneyPaymentComposeContent(isDarkMode = true) {
        ShowInputCode(
            AuthTypeState.SMS(2000, 5, 3, 2),
            "123",
            {},
            {},
            "test error",
            showProgress = false,
            {}
        )
    }
}