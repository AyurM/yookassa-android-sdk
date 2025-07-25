/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import ru.yoomoney.sdk.gui.gui.R as GuiResources

internal object InMemoryColorSchemeRepository {
    var colorScheme: ColorScheme = ColorScheme.getDefaultScheme()

    fun backgroundStateList(context: Context): ColorStateList {
        val color = intArrayOf(
            ContextCompat.getColor(context, GuiResources.color.color_ghost),
            colorScheme.primaryColor
        )
        return ColorStateList(stateArray(), color)
    }

    fun typeColorStateList(context: Context): ColorStateList {
        val color = intArrayOf(
            ContextCompat.getColor(context, GuiResources.color.color_type_ghost),
            colorScheme.primaryColor
        )
        return ColorStateList(stateArray(), color)
    }

    private fun stateArray() = arrayOf(
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled)
    )
}
