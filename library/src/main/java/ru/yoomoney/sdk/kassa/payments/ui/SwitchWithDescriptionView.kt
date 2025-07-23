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
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.databinding.YmSwitchWithDescriptionViewBinding

internal open class SwitchWithDescriptionView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: YmSwitchWithDescriptionViewBinding =
        YmSwitchWithDescriptionViewBinding.inflate(LayoutInflater.from(context), this, true)
    val switchView = binding.switchView
    var title: CharSequence?
        set(value) {
            binding.switchView.text = value
        }
        get() = binding.switchView.text

    var description: CharSequence?
        set(value) {
            binding.descriptionView.text = value
        }
        get() = binding.descriptionView.text

    var isChecked: Boolean
        set(value) {
            binding.switchView.isChecked = value
        }
        get() = binding.switchView.isChecked

    var movementMethod: MovementMethod
        set(value) {
            binding.switchView.movementMethod = value
        }
        get() = binding.switchView.movementMethod

    init {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ym_ListItemSwitchWithDescription, defStyleAttr, 0
        )

        attributes.getText(R.styleable.ym_ListItemSwitchWithDescription_ym_title)?.apply {
            title = this
        }

        attributes.getBoolean(R.styleable.ym_ListItemSwitchWithDescription_ym_isChecked, false).apply {
            isChecked = this
        }

        attributes.getText(R.styleable.ym_ListItemSwitchWithDescription_ym_description)?.apply {
            description = this
        }
    }
}

internal fun SwitchWithDescriptionView.onCheckedChangedListener(listener: ((Boolean) -> Unit)?) {
    listener?.let {
        switchView.setOnCheckedChangeListener { _, isChecked ->
            listener(isChecked)
        }
    } ?: switchView.setOnCheckedChangeListener(null)
}