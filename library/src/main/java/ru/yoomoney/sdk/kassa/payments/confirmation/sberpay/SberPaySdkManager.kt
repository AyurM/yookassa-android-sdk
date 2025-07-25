/*
 * The MIT License (MIT)
 * Copyright © 2024 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.confirmation.sberpay

import android.app.Application
import android.content.Context
import ru.yoomoney.sdk.kassa.payments.utils.logSberPaySdk
import spay.sdk.SPaySdkApp
import spay.sdk.SPaySdkInitConfig
import spay.sdk.api.SPayStage

internal object SberPaySdkManager {
    private var isSberSdkInitialized: Boolean = false

    internal fun init(app: Application) {
        if (isSberSdkInitialized.not()) {
            val config = SPaySdkInitConfig(
                application = app,
                enableBnpl = false,
                stage = SPayStage.Prod,
                resultViewNeeded = true,
                enableLogging = false,
                initializationResult = {initializationResult -> logSberPaySdk("init result: $initializationResult")}
            )
            SPaySdkApp.getInstance().initialize(config)
            isSberSdkInitialized = true
        }
    }

    internal fun isReadyForSPay(context: Context): Boolean {
        if (isSberSdkInitialized.not()) return false

        return SPaySdkApp.getInstance().isReadyForSPaySdk(context)
    }
}