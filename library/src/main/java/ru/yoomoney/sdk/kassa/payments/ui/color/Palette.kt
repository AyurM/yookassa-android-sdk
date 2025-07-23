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

package ru.yoomoney.sdk.kassa.payments.ui.color

import ru.yoomoney.sdk.guiCompose.theme.Palette
import ru.yoomoney.sdk.guiCompose.theme.YooColors

internal val MSDKTheme = Palette(
    light = YooColors.defaultColorsLight.copy(
        theme = YooColors.defaultColorsLight.theme.copy(
            tint = Colors.tint,
            tintBg = Colors.tintBg,
            tintCard = Colors.ghostTint,
            tintFade = Colors.fadeTint,
            tintGhost = Colors.ghostTint
        ),
        background = YooColors.defaultColorsLight.background.copy(
            default = Colors.default,
            divider = Colors.divider,
            actionRipple = Colors.actionRipple,
            card = Colors.card,
            border = Colors.boarder
        ),
        type = YooColors.defaultColorsLight.type.copy(
            primary = Colors.primary,
            secondary = Colors.secondary,
            inverse = Colors.inverse,
            ghost = Colors.secondaryInverse
        )
    ),
    dark = YooColors.defaultColorsDark.copy(
        theme = YooColors.defaultColorsDark.theme.copy(
            tint = Colors.tint,
            tintBg = Colors.tintBgDark,
            tintCard = Colors.ghostTint,
            tintFade =Colors.fadeTint,
            tintGhost = Colors.ghostTint
        ),
        background = YooColors.defaultColorsDark.background.copy(
            default = Colors.defaultDark,
            divider = Colors.dividerDark,
            actionRipple = Colors.actionRippleDark,
            card = Colors.cardDark,
            border = Colors.boarderDark
        ),
        type = YooColors.defaultColorsDark.type.copy(
            primary = Colors.primaryDark,
            secondary = Colors.secondaryDark,
            inverse = Colors.inverseDark,
            ghost = Colors.secondaryInverseDark
        )
    )
)