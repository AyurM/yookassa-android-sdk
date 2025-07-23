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

package ru.yoomoney.sdk.kassa.payments.threeDS

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.guiCompose.views.buttons.ButtonTestTags
import ru.yoomoney.sdk.guiCompose.views.topbar.TopBarDefault
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.gui.gui.R as GuiResources

@Composable
internal fun WebViewTopBar(
    onNavigationClick: () -> Unit,
    isProgress: Boolean,
) {
    TopBarDefault(
        onNavigationClick = onNavigationClick,
        navigationPainter = painterResource(id = R.drawable.ym_ic_close),
        content = {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                content = {}
            )
            if (isProgress) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag(ButtonTestTags.progress)
                        .size(YooTheme.dimens.spaceM),
                    strokeWidth = YooTheme.dimens.buttonProgressStrokeWidth,
                    color = YooTheme.colors.theme.tint
                )
            }
        },
        backgroundColor = colorResource(id = GuiResources.color.color_default)
    )
}

@Preview()
@Composable
internal fun WebViewTopbarPreview() {
    MoneyPaymentComposeContent {
        WebViewTopBar({}, false)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun WebViewTopbarPreviewDark() {
    MoneyPaymentComposeContent() {
        WebViewTopBar({}, false)
    }
}
