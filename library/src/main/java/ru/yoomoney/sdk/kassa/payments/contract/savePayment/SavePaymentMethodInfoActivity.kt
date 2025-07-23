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

package ru.yoomoney.sdk.kassa.payments.contract.savePayment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.core.text.HtmlCompat
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent

internal const val EXTRA_TITLE_RES = "ru.yoo.money.android.extra.SAVE_PAYMENT_INFO_TITLE"
internal const val EXTRA_TEXT_RES = "ru.yoo.money.android.extra.SAVE_PAYMENT_INFO_TEXT"

internal const val EXTRA_TITLE_STRING = "ru.yoo.money.android.extra.SAVE_PAYMENT_INFO_TITLE_STRING"
internal const val EXTRA_TEXT_STRING = "ru.yoo.money.android.extra.SAVE_PAYMENT_INFO_TEXT_STRING"

internal const val EXTRA_ADDITIONAL_TEXT_RES = "ru.yoo.money.android.extra.ADDITIONAL_TEXT_RES"

class SavePaymentMethodInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MoneyPaymentComposeContent {
                SavePaymentMethodScreen(
                    title = intent.getStringExtra(EXTRA_TITLE_STRING)
                        ?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
                        ?: resources.getString(intent.getIntExtra(EXTRA_TITLE_RES, 0)),
                    info = intent.getStringExtra(EXTRA_TEXT_STRING)
                        ?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
                        ?: resources.getString(intent.getIntExtra(EXTRA_TEXT_RES, 0)),
                    onBack = { finish() },
                    actionText = stringResource(id = R.string.ym_understand_button),
                    onButtonClick = { finish() },
                    additionalInfo = intent.getIntExtra(EXTRA_ADDITIONAL_TEXT_RES, 0).takeIf { it != 0 }?.let {
                        stringResource(id = it)
                    }
                )
            }
        }
    }

    companion object {
        fun create(context: Context, titleRes: Int, textRes: Int, additionalTextRes: Int?): Intent {
            return Intent(context, SavePaymentMethodInfoActivity::class.java).apply {
                putExtra(EXTRA_TITLE_RES, titleRes)
                putExtra(EXTRA_TEXT_RES, textRes)
                putExtra(EXTRA_ADDITIONAL_TEXT_RES, additionalTextRes)
            }
        }

        fun create(context: Context, title: String, text: String): Intent {
            return Intent(context, SavePaymentMethodInfoActivity::class.java).apply {
                putExtra(EXTRA_TITLE_STRING, title)
                putExtra(EXTRA_TEXT_STRING, text)
            }
        }
    }
}
