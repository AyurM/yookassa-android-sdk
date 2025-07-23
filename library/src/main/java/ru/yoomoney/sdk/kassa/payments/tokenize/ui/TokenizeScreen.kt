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

package ru.yoomoney.sdk.kassa.payments.tokenize.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.kassa.payments.ui.CustomDimens
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.LoadingStateScreen
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners

@Composable
internal fun TokenizeScreen(state: TokenizeState) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = CustomDimens.bottomDialogMinHeight)
            .roundBottomSheetCorners()
            .background(YooTheme.colors.theme.tintBg)
            .nestedScroll(rememberNestedScrollInteropConnection())
            .wrapContentHeight()
            .animateContentSize()
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is TokenizeState.Start -> {
                // Nothing
            }

            is TokenizeState.TokenizeError -> ErrorStateScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CustomDimens.bottomDialogMinHeight),
                subtitle = state.subtitle,
                action = state.actionText,
                onClick = state.onAction
            )

            is TokenizeState.Tokenize -> LoadingStateScreen(modifier = Modifier.height(CustomDimens.bottomDialogMinHeight))
        }
    }
}
