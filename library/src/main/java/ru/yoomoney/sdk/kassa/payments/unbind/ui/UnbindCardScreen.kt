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

package ru.yoomoney.sdk.kassa.payments.unbind.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.guiCompose.views.buttons.PrimaryColoredButtonView
import ru.yoomoney.sdk.guiCompose.views.notice.NoticeHost
import ru.yoomoney.sdk.guiCompose.views.notice.NoticeService
import ru.yoomoney.sdk.guiCompose.views.notice.rememberNoticeService
import ru.yoomoney.sdk.guiCompose.views.topbar.TopBarDefault
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.ui.color.Colors
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.ui.view.BankCardView
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners

@Composable
internal fun UnbindCardScreen(
    onCloseScreen: () -> Unit,
    onClickUnbind: (cardId: String) -> Unit,
    state: UnbindCardScreenUiState,
    hostState: SnackbarHostState,
    noticeService: NoticeService,
) {
    when (state) {
        is UnbindCardScreenUiState.Initial -> Initial(
            onCloseScreen = onCloseScreen,
            hostState = hostState,
            noticeService = noticeService
        )

        is UnbindCardScreenUiState.ContentLinkedBankCard -> ContentLinkedBankCard(
            state.instrumentBankCard,
            onCloseScreen,
            onClickUnbind,
            hostState,
            noticeService,
            state.showProgress
        )

        is UnbindCardScreenUiState.ContentLinkedWallet -> ContentLinkedWallet(
            state,
            onCloseScreen,
            onClickUnbind,
            hostState,
            noticeService
        )
    }
}

@Composable
private fun UnbindCardForm(
    showProgress: Boolean,
    cardNumber: String,
    onCloseScreen: () -> Unit,
    isWalletLinked: Boolean,
    onClickUnbind: () -> Unit,
    hostState: SnackbarHostState,
    noticeService: NoticeService,
) {
    Box {
        Column(Modifier.roundBottomSheetCorners()) {
            TopBarDefault(title = "•••• ${cardNumber.takeLast(4)}", onNavigationClick = onCloseScreen)
            Column(
                modifier = Modifier
                    .background(YooTheme.colors.theme.tintBg)
            ) {
                FilledBankCard(isWalletLinked, cardNumber)
                if (isWalletLinked.not()) {
                    PrimaryColoredButtonView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = YooTheme.dimens.spaceL,
                                start = YooTheme.dimens.spaceM,
                                end = YooTheme.dimens.spaceM
                            ),
                        text = stringResource(id = R.string.ym_unbind_card_action),
                        contentColor = Colors.alert,
                        backgroundColor = Colors.alert.copy(alpha = 0.15f),
                        onClick = onClickUnbind,
                        isProgress = showProgress,
                    )
                }
            }
        }
        NoticeHost(
            hostState = hostState,
            noticeService = noticeService,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FilledBankCard(
    isWalletLinked: Boolean,
    cardNumber: String,
) {
    val name = if (isWalletLinked) {
        stringResource(id = R.string.ym_linked_wallet_card)
    } else {
        stringResource(id = R.string.ym_linked_card)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = YooTheme.dimens.spaceM),
        factory = { context ->
            BankCardView(context).apply {
                isClickable = false
                setChangeCardAvailable(false)
                showBankLogo(cardNumber)
                setCardData(
                    cardNumber = cardNumber,
                    cardName = name
                )
                hideAdditionalInfo()
            }
        }
    )
}


@Composable
private fun Initial(
    onCloseScreen: () -> Unit,
    hostState: SnackbarHostState,
    noticeService: NoticeService,
) {
    UnbindCardForm(
        showProgress = true,
        cardNumber = "",
        onCloseScreen = onCloseScreen,
        isWalletLinked = false,
        onClickUnbind = { },
        hostState,
        noticeService
    )
}

@Composable
private fun ContentLinkedBankCard(
    instrumentBankCard: PaymentInstrumentBankCard,
    onCloseScreen: () -> Unit,
    onClickUnbind: (cardId: String) -> Unit,
    hostState: SnackbarHostState,
    noticeService: NoticeService,
    showProgress: Boolean,
) {
    val cardNumber = "${instrumentBankCard.first6}••••••${instrumentBankCard.last4}"
    UnbindCardForm(
        showProgress = showProgress,
        cardNumber = cardNumber,
        onCloseScreen = onCloseScreen,
        onClickUnbind = { onClickUnbind(instrumentBankCard.paymentInstrumentId) },
        isWalletLinked = false,
        hostState = hostState,
        noticeService = noticeService
    )
}

@Composable
private fun ContentLinkedWallet(
    state: UnbindCardScreenUiState.ContentLinkedWallet,
    onCloseScreen: () -> Unit,
    onClickUnbind: (cardId: String) -> Unit,
    hostState: SnackbarHostState,
    noticeService: NoticeService,
) {
    UnbindCardForm(
        showProgress = false,
        cardNumber = state.linkedCard.pan,
        onCloseScreen = onCloseScreen,
        onClickUnbind = { onClickUnbind(state.linkedCard.cardId) },
        isWalletLinked = true,
        hostState = hostState,
        noticeService = noticeService
    )
}

@Preview(showBackground = true)
@Composable
private fun UnbindCardScreenPreview() {
    val hostState = remember { SnackbarHostState() }
    val noticeService = rememberNoticeService(hostState)
    MoneyPaymentComposeContent {
        UnbindCardForm(
            false,
            "1234",
            {},
            false,
            {},
            hostState,
            noticeService
        )
    }
    noticeService.showSuccessNotice("test")
}