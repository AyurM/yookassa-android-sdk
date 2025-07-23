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

package ru.yoomoney.sdk.kassa.payments.http

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.ConnectionPool
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.kassa.payments.utils.isTestingBuild
import ru.yoomoney.sdk.yoopinning.PinningHelper
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

private const val DEFAULT_TIMEOUT: Long = 30

private fun baseHttpClientBuilder(
    context: Context,
    showLogs: Boolean,
    certsHost: String?,
    isDevHost: Boolean
): OkHttpClient.Builder = OkHttpClient.Builder()
    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(4, 10L, TimeUnit.MINUTES))
    .followSslRedirects(false)
    .followRedirects(false)
    .applySsl(context, certsHost, isDevHost)
    .addUserAgent(context)
    .applyLogging(showLogs)

internal fun newHttpClient(
    context: Context,
    showLogs: Boolean,
    certsHost: String?,
    isDevHost: Boolean
): OkHttpClient = baseHttpClientBuilder(context, showLogs, certsHost, isDevHost).build()

internal fun newAuthorizedHttpClient(
    context: Context,
    showLogs: Boolean,
    certsHost: String?,
    isDevHost: Boolean,
    shopToken: String,
    tokensStorage: TokensStorage,
): OkHttpClient = baseHttpClientBuilder(context, showLogs, certsHost, isDevHost)
    .applyAuthorization(shopToken)
    .applyPassportAuthorization(tokensStorage)
    .build()

internal fun authPaymentsHttpClient(
    context: Context,
    showLogs: Boolean,
    certsHost: String?,
    isDevHost: Boolean,
    shopToken: String,
    tokensStorage: TokensStorage,
):  OkHttpClient = baseHttpClientBuilder(context, showLogs, certsHost, isDevHost)
    .addUserAgent(context)
    .applyRequestType()
    .applyXForwardedFor()
    .applyMerchantClientAuthorization(shopToken)
    .applyPaymentsAuthorization(tokensStorage)
    .build()

private fun OkHttpClient.Builder.applyRequestType() =
    addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("Content-Type", "application/json")
            .build()
        chain.proceed(request)

    }

private fun OkHttpClient.Builder.applyXForwardedFor() =
    addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("X-Forwarded-For", "127.0.0.1")
            .build()
        chain.proceed(request)

    }

private fun OkHttpClient.Builder.applyMerchantClientAuthorization(shopToken: String) =
    addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("Merchant-Client-Authorization", Credentials.basic(shopToken, ""))
            .build()
        chain.proceed(request)

    }

private fun OkHttpClient.Builder.applyPaymentsAuthorization(tokensStorage: TokensStorage) = addInterceptor { chain ->
    val request = chain.request()
        .newBuilder()
        .header("Authorization", tokensStorage.userAuthToken?.createBearerHeader() ?: "")
        .build()
    chain.proceed(request)
}

private fun OkHttpClient.Builder.applyAuthorization(shopToken: String) = addInterceptor { chain ->
    val request = chain.request()
        .newBuilder()
        .header("Authorization", Credentials.basic(shopToken, ""))
        .build()
    chain.proceed(request)
}

private fun OkHttpClient.Builder.applyPassportAuthorization(
    tokensStorage: TokensStorage,
) = addInterceptor { chain ->
    val userAuthToken: String? =
        if (!tokensStorage.passportAuthToken.isNullOrEmpty() && !tokensStorage.paymentAuthToken.isNullOrEmpty()) {
            tokensStorage.passportAuthToken
        } else {
            tokensStorage.userAuthToken
        }
    if (userAuthToken != null) {
        val request = chain.request()
            .newBuilder()
            .header("Passport-Authorization", userAuthToken)
            .build()
        chain.proceed(request)
    } else {
        chain.proceed(chain.request())
    }
}

internal fun OkHttpClient.Builder.applyLogging(
    showLogs: Boolean
) = if (isTestingBuild() && showLogs) {
    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
} else {
    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE))
}

internal fun OkHttpClient.Builder.applySsl(
    context: Context,
    certsHost: String?,
    isDevHost: Boolean
): OkHttpClient.Builder {
    val trustManager: X509TrustManager = getTrustManager(context, certsHost, isTestingBuild())
    val sslSocketFactory: SSLSocketFactory = getSSLSocketFactoryAndInit(trustManager)
    sslSocketFactory(sslSocketFactory, trustManager)
    if (isDevHost) {
        hostnameVerifier { _, _ -> true }
    }
    return this
}

private fun getSSLSocketFactoryAndInit(trustManager: X509TrustManager) =
    SSLContext.getInstance("SSL").apply { init(null, arrayOf(trustManager), SecureRandom()) }.socketFactory

private val debugTrustManager = (
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // does nothing
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                // does nothing
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            // Called reflectively by X509TrustManagerExtensions.
            fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, host: String) =
                Unit
        }
        )

internal fun getTrustManager(context: Context, certsHost: String?, isDebug: Boolean): X509TrustManager =
    if (certsHost.isNullOrBlank() || isDebug) {
        debugTrustManager
    } else {
        PinningHelper.getInstance(context, certsHost).trustManager
    }