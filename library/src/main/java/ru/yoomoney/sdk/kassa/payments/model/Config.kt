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

package ru.yoomoney.sdk.kassa.payments.model

import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.HostParameters

internal data class Config(
    val yooMoneyLogoUrlLight: String,
    val yooMoneyLogoUrlDark: String,
    val paymentMethods: List<ConfigPaymentOption>,
    val savePaymentMethodOptionTexts: SavePaymentMethodOptionTexts,
    val userAgreementUrl: String,
    val googlePayGateway: String,
    val yooMoneyApiEndpoint: String,
    val yooMoneyPaymentAuthorizationApiEndpoint: String,
    val yooMoneyAuthApiEndpoint: String?,
    val agentSchemeProviderAgreement: Map<String, String>?,
)

internal fun String.toConfig(hostParameters: HostParameters): Config {
    val config = jacksonBaseObjectMapper.readValue(this, Config::class.java)
    return if (hostParameters.isDevHost) {
        return config.copy(
            yooMoneyApiEndpoint = hostParameters.host,
            yooMoneyPaymentAuthorizationApiEndpoint = hostParameters.paymentAuthorizationHost,
            yooMoneyAuthApiEndpoint = hostParameters.authHost
        )
    } else {
        config
    }
}

internal fun Config.toJsonObject(): String =
    jacksonBaseObjectMapper.writeValueAsString(this)