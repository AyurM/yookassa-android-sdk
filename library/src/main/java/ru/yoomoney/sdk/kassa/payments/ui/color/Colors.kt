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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

internal object Colors {

    // tint colors
    internal val tint = Color(InMemoryColorSchemeRepository.colorScheme.primaryColor)
    internal val tintBg = Color(0xFFFFFFFF)
    internal val tintBgDark = Color(0xFF0E1930)
    internal val fadeTint = getFadeTint(tint.toArgb())
    internal val ghostTint =  getGhostTint(tint.toArgb())
    // background colors
    internal val default = Color(0xFFFFFFFF)
    internal val defaultDark = Color(0xFF0E1930)
    internal val divider = Color(0xFFF2F4F7)
    internal val dividerDark = Color(0x33FFFFFF)
    internal val card = Color(0xFFFFFFFF)
    internal val cardDark = Color(0xFF0E1930)
    internal val boarder = Color(0xFFFFFFFF)
    internal val boarderDark = Color(0x1AFFFFFF)

    // type colors
    internal val primary = Color(0xFF0A2540)
    internal val primaryDark = Color(0xFFFFFFFF)
    internal val secondary = Color(0xB30A2540)
    internal val secondaryDark = Color(0xB3FFFFFF)
    internal val secondaryInverse = Color(0xB30A2540)
    internal val secondaryInverseDark = Color(0xB3FFFFFF)
    internal val inverse = Color(0xFFFFFFFF)
    internal val inverseDark = Color(0xFFFFFFFF)
    internal val actionRipple = Color(0xFFE4E9F1)
    internal val actionRippleDark = Color(0xFF17294F)

    internal val alert = Color(0xFFF00000)
}