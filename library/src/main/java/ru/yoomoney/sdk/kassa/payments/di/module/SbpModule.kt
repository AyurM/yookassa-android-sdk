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
import retrofit2.Retrofit
import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.api.SBPApi
import ru.yoomoney.sdk.kassa.payments.api.SuspendResultCallAdapterFactory
import ru.yoomoney.sdk.kassa.payments.api.YooKassaJacksonConverterFactory
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.di.AuthorizedHttpClient
import ru.yoomoney.sdk.kassa.payments.di.scope.ConfirmationScope
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListInteractor
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListInteractorImpl
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListRepository
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListRepositoryImpl

@Module
internal class SbpModule {

    @ConfirmationScope
    @Provides
    fun sbpApi(
        hostProvider: HostProvider,
        @AuthorizedHttpClient okHttpClient: OkHttpClient,
        apiErrorMapper: ApiErrorMapper,
    ): SBPApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(hostProvider.host() + "/")
            .addConverterFactory(YooKassaJacksonConverterFactory.create(jacksonBaseObjectMapper))
            .addCallAdapterFactory(SuspendResultCallAdapterFactory(apiErrorMapper))
            .build()
            .create(SBPApi::class.java)
    }

    @ConfirmationScope
    @Provides
    fun provideBankListRepository(sbpApi: SBPApi, paymentsApi: PaymentsApi): BankListRepository = BankListRepositoryImpl(sbpApi, paymentsApi)

    @ConfirmationScope
    @Provides
    fun provideBankListInteractor(bankListRepository: BankListRepository): BankListInteractor =
        BankListInteractorImpl(bankListRepository)
}
