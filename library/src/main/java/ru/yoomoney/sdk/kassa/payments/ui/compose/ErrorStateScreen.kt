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

package ru.yoomoney.sdk.kassa.payments.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.guiCompose.views.emptystates.EmptyStateLargeView
import ru.yoomoney.sdk.kassa.payments.R

@Composable
internal fun ErrorStateScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    drawableResource: Int = R.drawable.ym_ic_close,
    action: String? = null,
    title: String? = stringResource(id = R.string.ym_error_screen_title),
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    EmptyStateLargeView(
        modifier = modifier.background(YooTheme.colors.theme.tintBg),
        painter = painterResource(drawableResource),
        subtitle = subtitle,
        action = action,
        title = title,
        scaleIcon = true,
        onClick = onClick
    )
}

@Preview
@Composable
private fun ErrorStateScreenPreview() {
    MoneyPaymentComposeContent(isDarkMode = false) {
        ErrorStateScreen(
            action = "try again",
            title = "unknown error",
            subtitle = "follow the instuctions, please",
            onClick = {})
    }
}

@Preview
@Composable
private fun ErrorStateScreenPreviewDark() {
    MoneyPaymentComposeContent(isDarkMode = true) {
        ErrorStateScreen(
            action = "try again",
            title = "unknown error",
            subtitle = "follow the instuctions, please",
            onClick = {})
    }
}
