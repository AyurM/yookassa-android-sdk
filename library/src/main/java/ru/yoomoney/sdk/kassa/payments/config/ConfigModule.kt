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

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.yoomoney.sdk.kassa.payments.api.YooKassaJacksonConverterFactory
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.api.config.ConfigRequestApi
import ru.yoomoney.sdk.kassa.payments.api.SuspendResultCallAdapterFactory
import ru.yoomoney.sdk.kassa.payments.api.config.ConfigApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.model.toConfig
import ru.yoomoney.sdk.kassa.payments.utils.readText
import javax.inject.Singleton

@Module
internal class ConfigModule {

    @Singleton
    @Provides
    fun configRepository(
        context: Context,
        errorReporter: ErrorLoggerReporter,
        testParameters: TestParameters,
        okHttpClient: OkHttpClient,
        apiErrorMapper: ApiErrorMapper
    ): ConfigRepository {
        val defaultConfig = context.resources.openRawResource(R.raw.ym_default_config).readText().toConfig(testParameters.hostParameters)
        return if (testParameters.mockConfiguration != null) {
            MockConfigRepository(defaultConfig)
        } else {
            ApiConfigRepository(
                hostParameters = testParameters.hostParameters,
                getDefaultConfig = defaultConfig,
                sp = context.getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE),
                errorReporter = errorReporter,
                configRequestApi = createConfigApi(
                    { testParameters.hostParameters.configHost + "/" },
                    okHttpClient,
                    ConfigApiErrorMapper(jacksonBaseObjectMapper, apiErrorMapper)
                )
            )
        }
    }

    private fun createConfigApi(
        hostProvider: () -> String,
        httpClient: OkHttpClient,
        apiErrorMapper: ApiErrorMapper
    ): ConfigRequestApi {
        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(hostProvider())
            .addConverterFactory(YooKassaJacksonConverterFactory.create(jacksonBaseObjectMapper))
            .addCallAdapterFactory(SuspendResultCallAdapterFactory(apiErrorMapper))
            .build()
            .create(ConfigRequestApi::class.java)
    }

    companion object {
        internal const val CONFIG_PREFS = "configPrefs"
    }
}