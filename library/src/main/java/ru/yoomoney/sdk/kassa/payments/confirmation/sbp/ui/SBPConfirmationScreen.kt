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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import kotlinx.coroutines.channels.ReceiveChannel
import ru.yoomoney.sdk.guiCompose.views.notice.Notice
import ru.yoomoney.sdk.guiCompose.views.notice.rememberNoticeService
import ru.yoomoney.sdk.kassa.payments.ui.compose.CustomDimens
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.LoadingStateScreen
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners
import ru.yoomoney.sdk.marchcompose.extensions.observe

@Composable
internal fun SBPConfirmationScreen(
    state: SBPConfirmationState,
    notices: ReceiveChannel<Notice>,
) {
    val hostState = remember { SnackbarHostState() }
    val noticeService = rememberNoticeService(hostState = hostState)

    notices.observe { notice ->
        notice.show(noticeService)
    }

    Box(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .animateContentSize()
            .roundBottomSheetCorners(),
    ) {
        when (state) {
            is SBPConfirmationState.Loading -> LoadingScreen()
            is SBPConfirmationState.InitialError -> ErrorStateScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CustomDimens.bottomDialogMaxHeight),
                subtitle = state.failureTitle,
                action = state.actionText,
                onClick = state.onAction
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CustomDimens.bottomDialogMaxHeight), contentAlignment = Alignment.Center
    ) {
        LoadingStateScreen()
    }
}
