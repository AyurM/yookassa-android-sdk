/*
 * The MIT License (MIT)
 * Copyright © 2024 NBCO YooMoney LLC
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
import androidx.core.graphics.ColorUtils
import android.graphics.Color as ColorAndroid

private const val HSV_ARRAY_SIZE = 3
private const val HSV_SATURATION_INDEX = 1
private const val HSV_BRIGHTNESS_INDEX = 2
private const val COLOR_GHOST_TINT_ALPHA = 33

private const val COLOR_FADE_TINT_SATURATION_MULTIPLIER = 0.5f
private const val COLOR_FADE_TINT_BRIGHTNESS_MULTIPLIER = 0.9f

internal fun getFadeTint(accentColor: Int): Color {
    val hsv = FloatArray(HSV_ARRAY_SIZE)
    ColorAndroid.colorToHSV(accentColor, hsv)
    hsv[HSV_SATURATION_INDEX] = hsv[HSV_SATURATION_INDEX] * COLOR_FADE_TINT_SATURATION_MULTIPLIER
    hsv[HSV_BRIGHTNESS_INDEX] = hsv[HSV_BRIGHTNESS_INDEX] * COLOR_FADE_TINT_BRIGHTNESS_MULTIPLIER
    return Color(android.graphics.Color.HSVToColor(hsv))
}

internal fun getGhostTint(accentColor: Int) : Color {
    return Color(ColorUtils.setAlphaComponent(accentColor, COLOR_GHOST_TINT_ALPHA))
}