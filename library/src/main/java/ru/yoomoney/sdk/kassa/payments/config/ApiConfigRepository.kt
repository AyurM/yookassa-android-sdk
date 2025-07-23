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

package ru.yoomoney.sdk.kassa.payments.config

import android.content.SharedPreferences
import ru.yoomoney.sdk.kassa.payments.api.config.ConfigRequestApi
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.HostParameters
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.model.Config
import ru.yoomoney.sdk.kassa.payments.model.SdkException
import ru.yoomoney.sdk.kassa.payments.model.mapper.mapToConfigModel
import ru.yoomoney.sdk.kassa.payments.model.toConfig
import ru.yoomoney.sdk.kassa.payments.model.toJsonObject
import ru.yoomoney.sdk.kassa.payments.utils.getLanguage

private const val CONFIG_FIELD_NAME = "config"

internal class ApiConfigRepository(
    private val hostParameters: HostParameters,
    private val getDefaultConfig: Config,
    private val configRequestApi: ConfigRequestApi,
    private val sp: SharedPreferences,
    private val errorReporter: ErrorLoggerReporter
) : ConfigRepository {

   private var cachedConfig: Config
    set(value) {
        sp.edit().putString(CONFIG_FIELD_NAME + "_${getLanguage()}", value.toJsonObject()).apply()
    }

    get() = sp.getString(CONFIG_FIELD_NAME + "_${getLanguage()}", null)?.let {
        try {
            it.toConfig(hostParameters)
        } catch (e: Throwable) {
            errorReporter.report(SdkException(e))
            getDefaultConfig
        }
    } ?: getDefaultConfig

    override fun getConfig(): Config = cachedConfig

    override suspend fun loadConfig(): Result<Config> {
        configRequestApi.getConfig(getLanguage()).onSuccess {
            cachedConfig = it.mapToConfigModel()
        }
        return Result.success(cachedConfig)
    }
}