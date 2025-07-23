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

package ru.yoomoney.sdk.kassa.payments.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.di.AuthPaymentsHttpClient
import ru.yoomoney.sdk.kassa.payments.di.AuthorizedHttpClient
import ru.yoomoney.sdk.kassa.payments.di.scope.MainScope
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.http.HostProviderImpl
import ru.yoomoney.sdk.kassa.payments.http.authPaymentsHttpClient
import ru.yoomoney.sdk.kassa.payments.http.newAuthorizedHttpClient
import ru.yoomoney.sdk.kassa.payments.http.newHttpClient
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage
import ru.yoomoney.sdk.yoopinning.PinningHelper

private const val CERTS_HOST: String = "https://yoomoney.ru"

@Module
internal open class OkHttpModule {

    @MainScope
    @Provides
    fun hostProvider(
        testParameters: TestParameters,
        configRepository: ConfigRepository,
    ): HostProvider {
        return HostProviderImpl(configRepository, testParameters.hostParameters)
    }

    @MainScope
    @Provides
    open fun okHttpClient(context: Context, testParameters: TestParameters): OkHttpClient {
        return newHttpClient(
            context = context,
            showLogs = testParameters.showLogs,
            certsHost = PinningHelper.prepareCertsHostFromBaseUrl(CERTS_HOST),
            isDevHost = testParameters.hostParameters.isDevHost
        )
    }

    @MainScope
    @Provides
    @AuthorizedHttpClient
    open fun authorizedOkHttpClient(
        context: Context,
        testParameters: TestParameters,
        clientApplicationKey: String,
        tokensStorage: TokensStorage,
    ): OkHttpClient {
        return newAuthorizedHttpClient(
            context = context,
            showLogs = testParameters.showLogs,
            certsHost = PinningHelper.prepareCertsHostFromBaseUrl(CERTS_HOST),
            isDevHost = testParameters.hostParameters.isDevHost,
            shopToken = clientApplicationKey,
            tokensStorage = tokensStorage
        )
    }

    @MainScope
    @Provides
    @AuthPaymentsHttpClient
    open fun authPaymentsOkHttpClient(
        context: Context,
        testParameters: TestParameters,
        clientApplicationKey: String,
        tokensStorage: TokensStorage,
    ): OkHttpClient {
        return authPaymentsHttpClient(
            context = context,
            showLogs = testParameters.showLogs,
            certsHost = PinningHelper.prepareCertsHostFromBaseUrl(CERTS_HOST),
            isDevHost = testParameters.hostParameters.isDevHost,
            shopToken = clientApplicationKey,
            tokensStorage = tokensStorage
        )
    }
}