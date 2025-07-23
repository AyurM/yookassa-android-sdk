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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import io.appmetrica.analytics.AppMetrica
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.logging.ReporterLogger
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaReporter
import ru.yoomoney.sdk.kassa.payments.threeDS.WebViewActivity.Companion.checkUrl
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent

private const val KEY_LOAD_URL = "loadUrl"
private const val KEY_REDIRECT_URL = "returnUrl"

private const val ACTION_CLOSE_3DS_SCREEN = "close3dsScreen"

@Keep
internal class WebViewFragment : Fragment(), WebViewListener {

    private var webView: WebView? = null

    private val loadUrl: String by lazy {
        arguments?.getString(KEY_LOAD_URL).orEmpty()
    }
    private val redirectUrl: String? by lazy {
        arguments?.getString(KEY_REDIRECT_URL)
    }

    private val progress = MutableLiveData<Boolean>()

    private lateinit var reporterLogger: ReporterLogger

    init {
        retainInstance = true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reporterLogger = ReporterLogger(
            AppMetricaReporter(AppMetrica.getReporter(requireContext().applicationContext, BuildConfig.APP_METRICA_KEY)),
            (requireNotNull(arguments?.getParcelable(EXTRA_TEST_PARAMETERS)) as TestParameters).showLogs,
            requireContext()
        )
        val logParam = arguments?.getString(EXTRA_LOG_PARAM)

        if (savedInstanceState == null && logParam != null) {
            reporterLogger.report(logParam)
        }

        val context = checkNotNull(context) { "Context should be present here" }
        val appContext = context.applicationContext

        webView = WebView(appContext, null, 0).apply {
            isFocusableInTouchMode = true
            settings.javaScriptEnabled = true
            @Suppress("DEPRECATION")
            settings.saveFormData = false
            webViewClient = WebViewClientImpl(redirectUrl, this@WebViewFragment)
            webChromeClient = WebChromeClient()
        }
        if (checkUrl(loadUrl).not()) {
            onError(Checkout.ERROR_NOT_HTTPS_URL, "Not https:// url", loadUrl)
        }
        load(loadUrl)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                    } else {
                        onCloseActivity()
                        requireActivity().finish()
                    }
                }
            }
        )
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            removeAllViews()
            addView(ComposeView(requireContext()).apply {
                setContent {
                    val progressState = progress.observeAsState(initial = false).value
                    MoneyPaymentComposeContent {
                        WebViewTopBar(onNavigationClick = { close3DSProcess() }, isProgress = progressState)
                    }
                }
            })
            addView(webView)
        }
    }

    override fun onCloseActivity() {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
    }

    override fun onShowProgress() {
        progress.value = true
    }

    override fun onHideProgress() {
        progress.value = false
    }

    override fun onError(errorCode: Int, description: String?, failingUrl: String?) {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
        requireActivity().run {
            setResult(
                Checkout.RESULT_ERROR,
                Intent()
                    .putExtras(intent)
                    .putExtra(Checkout.EXTRA_ERROR_CODE, errorCode)
                    .putExtra(Checkout.EXTRA_ERROR_DESCRIPTION, description)
                    .putExtra(Checkout.EXTRA_ERROR_FAILING_URL, failingUrl)
            )
            finish()
        }
    }

    override fun onSuccess() {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, true)
        requireActivity().run {
            setResult(AppCompatActivity.RESULT_OK, Intent().putExtras(intent))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.also { (it.parent as ViewGroup?)?.removeView(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(KEY_LOAD_URL, loadUrl)
            putString(KEY_REDIRECT_URL, redirectUrl)
        }
    }

    private fun close3DSProcess() {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
        requireActivity().run {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun load(url: String) {
        val webView = checkNotNull(webView) { "load should be called after fragment initialization" }
        webView.loadUrl(url)
    }

    companion object {
        fun create(loadUrl: String, redirectUrl: String, testParameters: TestParameters, logParam: String? = null) = WebViewFragment().apply {
            arguments = bundleOf(KEY_LOAD_URL to loadUrl, KEY_REDIRECT_URL to redirectUrl, EXTRA_TEST_PARAMETERS to testParameters).apply {
                if (logParam != null) {
                   putString(EXTRA_LOG_PARAM, logParam)
                }
            }
        }
    }
}

internal interface WebViewListener {
    fun onShowProgress()
    fun onHideProgress()
    fun onError(errorCode: Int, description: String?, failingUrl: String?)
    fun onSuccess()
    fun onCloseActivity()
}