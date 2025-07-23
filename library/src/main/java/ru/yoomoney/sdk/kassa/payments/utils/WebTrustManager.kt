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

package ru.yoomoney.sdk.kassa.payments.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal interface WebTrustManager {
    /**
     * check certificate from error
     */
    fun checkCertificate(context: Context, error: SslError): Boolean
}

internal class WebTrustManagerImpl : WebTrustManager {

    @SuppressLint("DiscouragedPrivateApi")
    override fun checkCertificate(context: Context, error: SslError): Boolean {
        var passVerify = false
        if (error.primaryError != SslError.SSL_UNTRUSTED) {
            return passVerify
        }
        val cert = error.certificate
        runCatching {
            val field: Field = cert.javaClass.getDeclaredField("mX509Certificate")
            field.isAccessible = true
            val x509 = field.get(cert) as X509Certificate
            val chain = arrayOf(x509)
            val trustManagerFactory = initTrustStore(context)
            for (trustManager in trustManagerFactory.trustManagers) {
                if (trustManager is X509TrustManager) {
                    val x509TrustManager: X509TrustManager = trustManager as X509TrustManager
                    try {
                        x509TrustManager.checkServerTrusted(chain, "generic")
                        passVerify = true
                        break
                    } catch (e: Exception) {
                        passVerify = false
                    }
                }
            }
        }
        return passVerify
    }

    private fun initTrustStore(context: Context): TrustManagerFactory {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val certNames = listOf("ym_root_ca", "ym_sub_ca")
        val certsIns = certNames.map { name -> readPemCert(context, name) }
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            certsIns.forEachIndexed { index, certIns ->
                val cert = certIns.use { cf.generateCertificate(it) }
                setCertificateEntry(certNames[index], cert)
            }
        }
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)
        return tmf
    }

    private fun readPemCert(contex: Context, certName: String): InputStream {
        return fromPem(getPemAsString(contex, certName).trimIndent())
    }

    private fun getPemAsString(contex: Context, certName: String): String {
        val ins = contex.resources.openRawResource(
            contex.resources.getIdentifier(
                certName,
                "raw",
                contex.packageName
            )
        )
        return String(ins.readBytes(), StandardCharsets.ISO_8859_1)
    }

    private fun fromPem(pem: String): InputStream {
        val base64cert = pem.pemKeyContent()
        return fromBase64String(base64cert)
    }

    private fun String.pemKeyContent(): String =
        replace("\\s+", "")
            .replace("\n", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")

    private fun fromBase64String(base64cert: String): InputStream {
        val decoded = Base64.decode(base64cert, Base64.NO_WRAP)
        return ByteArrayInputStream(decoded)
    }
}
