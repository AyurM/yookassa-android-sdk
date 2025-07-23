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

package ru.yoomoney.sdk.kassa.payments.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import ru.yoomoney.sdk.gui.utils.extensions.tint
import ru.yoomoney.sdk.kassa.payments.databinding.YmDialogTopBarBinding
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

internal class DialogTopBar
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: YmDialogTopBarBinding = YmDialogTopBarBinding.inflate(LayoutInflater.from(context), this, true)

    var title: CharSequence?
        set(value) {
            binding.paymentTitle.text = value
        }
        get() = binding.paymentTitle.text

    var isLogoVisible: Boolean
        set(value) {
            binding.logo.isVisible = value
        }
        get() = binding.logo.isVisible

    val backButton: ImageView = binding.backButton

    val logo: ImageView = binding.logo

    init {
        val primaryColor = InMemoryColorSchemeRepository.colorScheme.primaryColor
        binding.backButton.setImageDrawable(binding.backButton.drawable.tint(primaryColor))

        val set = intArrayOf(android.R.attr.text)
        val a = context.obtainStyledAttributes(attrs, set)
        title = a.getText(0)
    }

    fun onBackButton(listener: ((View) -> Unit)?) {
        binding.backButton.isVisible = listener != null
        binding.backButton.setOnClickListener(listener)
    }

    fun onBackButton(listener: ((View) -> Unit)?, withAnimation: Boolean) {
        binding.backButton.isVisible = listener != null
        if (withAnimation) {
            TransitionManager.beginDelayedTransition(binding.dialogTopbarRoot)
        }
        binding.backButton.setOnClickListener(listener)
    }
}