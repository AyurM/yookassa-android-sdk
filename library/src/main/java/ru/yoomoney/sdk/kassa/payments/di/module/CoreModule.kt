/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.di.module

import android.content.Context
import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.api.SuspendResultCallAdapterFactory
import ru.yoomoney.sdk.kassa.payments.api.YooKassaJacksonConverterFactory
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.failures.DefaultApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.AuthorizedHttpClient
import ru.yoomoney.sdk.kassa.payments.di.scope.MainScope
import ru.yoomoney.sdk.kassa.payments.errorFormatter.DefaultErrorFormatter
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.model.Executor
import ru.yoomoney.sdk.kassa.payments.navigation.MainDialogRouter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.yooprofiler.YooProfiler
import ru.yoomoney.sdk.yooprofiler.YooProfilerHelper
import javax.inject.Singleton

@Module
internal class CoreModule {

    @Provides
    @Singleton
    fun mainExecutor(): Executor {
        val mainHandler = Handler(Looper.getMainLooper())
        return object : Executor {
            override fun invoke(p1: () -> Unit) {
                mainHandler.post(p1)
            }
        }
    }

    @Provides
    @MainScope
    fun paymentsApi(
        hostProvider: HostProvider,
        @AuthorizedHttpClient okHttpClient: OkHttpClient,
        apiErrorMapper: ApiErrorMapper,
    ): PaymentsApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(hostProvider.host() + "/")
            .addConverterFactory(YooKassaJacksonConverterFactory.create(jacksonBaseObjectMapper))
            .addCallAdapterFactory(SuspendResultCallAdapterFactory(apiErrorMapper))
            .build()
            .create(PaymentsApi::class.java)
    }

    @Provides
    @Singleton
    fun defaultErrorFormatter(context: Context): ErrorFormatter {
        return DefaultErrorFormatter(context)
    }

    @Provides
    @Singleton
    fun profilingSessionIdStorage(): ProfilingSessionIdStorage {
        return ProfilingSessionIdStorage()
    }

    @Provides
    @Singleton
    fun provideYooProfiler(context: Context): YooProfiler {
        return YooProfilerHelper.create(context)
    }

    @Provides
    @Singleton
    fun provideRouter(
        context: Context, testParameters: TestParameters
    ): Router {
        return MainDialogRouter(context, testParameters.showLogs)
    }

    @Provides
    @Singleton
    fun defaultApiErrorMapper(): ApiErrorMapper {
        return DefaultApiErrorMapper(jacksonBaseObjectMapper)
    }
}
