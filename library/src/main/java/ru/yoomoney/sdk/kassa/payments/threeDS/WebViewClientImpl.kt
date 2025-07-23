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

import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.utils.WebTrustManager
import ru.yoomoney.sdk.kassa.payments.utils.WebTrustManagerImpl
import ru.yoomoney.sdk.kassa.payments.utils.isBuildDebug

private val TAG = WebViewFragment::class.java.simpleName

internal class WebViewClientImpl(
    private val redirectUrl: String?,
    private val listener: WebViewListener?,
    private val webViewTrustManager: WebTrustManager = WebTrustManagerImpl()
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        listener?.onShowProgress()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        listener?.onHideProgress()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val redirectUrl = checkNotNull(redirectUrl) { "returnUrl should be present" }

        if (url.startsWith(redirectUrl)) {
            Log.d(TAG, "success: $url")
            listener?.onSuccess()
            view.stopLoading()
        } else {
            view.loadUrl(url)
        }
        return true
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError) {
        val certificateIsTrusted = webViewTrustManager.checkCertificate(view.context, error)
        if (BuildConfig.DEBUG && isBuildDebug() || certificateIsTrusted) {
            handler?.proceed()
        } else {
            super.onReceivedSslError(view, handler, error)
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        listener?.apply {
            onHideProgress()
            takeUnless { errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT }
                ?.onError(errorCode, description, failingUrl)
        }
    }
}