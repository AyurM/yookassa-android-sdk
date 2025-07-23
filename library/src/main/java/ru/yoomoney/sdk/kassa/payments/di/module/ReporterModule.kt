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
import dagger.Module
import dagger.Provides
import io.appmetrica.analytics.IReporter
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.logging.ReporterLogger
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorLoggerReporter
import ru.yoomoney.sdk.kassa.payments.metrics.ErrorReporter
import ru.yoomoney.sdk.kassa.payments.metrics.ExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SessionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaErrorReporter
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaExceptionReporter
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaLoggerReporter
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaReporter
import ru.yoomoney.sdk.kassa.payments.metrics.AppMetricaSessionReporter
import javax.inject.Singleton

@Module
internal class ReporterModule {

    @Provides
    @Singleton
    fun reporter(
        metrica: IReporter,
        testParameters: TestParameters,
        context: Context
    ): Reporter {
        return ReporterLogger(AppMetricaReporter(metrica), testParameters.showLogs, context)
    }

    @Provides
    @Singleton
    fun errorReporter(metrica: IReporter): ErrorReporter {
        return AppMetricaErrorReporter(metrica)
    }

    @Provides
    @Singleton
    fun errorLoggerReporter(
        testParameters: TestParameters,
        metrica: IReporter
    ): ErrorLoggerReporter {
        return AppMetricaLoggerReporter(
            testParameters.showLogs,
            AppMetricaErrorReporter(metrica)
        )
    }

    @Provides
    @Singleton
    fun exceptionReporter(metrica: IReporter): ExceptionReporter {
        return AppMetricaExceptionReporter(metrica)
    }

    @Provides
    @Singleton
    fun sessionReporter(metrica: IReporter): SessionReporter {
        return AppMetricaSessionReporter(metrica)
    }

}
