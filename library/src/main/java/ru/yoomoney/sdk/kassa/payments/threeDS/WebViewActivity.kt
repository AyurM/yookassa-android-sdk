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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.utils.DEFAULT_REDIRECT_URL

const val EXTRA_URL = "ru.yoomoney.sdk.kassa.payments.extra.URL"
const val EXTRA_TEST_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.TEST_PARAMETERS"
const val EXTRA_LOG_PARAM = "ru.yoomoney.sdk.kassa.payments.extra.LOG_PARAM"

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ym_activity_web_view)

        val url = requireNotNull(intent.getStringExtra(EXTRA_URL))
        val logParam = intent.getStringExtra(EXTRA_LOG_PARAM)
        val testParameters = requireNotNull(intent.getParcelableExtra<TestParameters>(EXTRA_TEST_PARAMETERS))

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.webview_container, WebViewFragment.create(url, DEFAULT_REDIRECT_URL, testParameters, logParam))
                .commit()
        }
    }

    companion object {
        fun create(
            context: Context,
            url: String,
            logParam: String? = null,
            testParameters: TestParameters,
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TEST_PARAMETERS, testParameters)
                if (logParam != null) {
                    putExtra(EXTRA_LOG_PARAM, logParam)
                }
            }
        }

        fun checkUrl(url: String) = URLUtil.isHttpsUrl(url) || BuildConfig.DEBUG && URLUtil.isAssetUrl(url)
    }
}