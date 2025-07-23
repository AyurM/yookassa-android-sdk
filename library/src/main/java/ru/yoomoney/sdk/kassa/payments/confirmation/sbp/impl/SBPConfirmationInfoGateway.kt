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

package ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.api.model.confirmationdata.ConfirmationDataResponse
import ru.yoomoney.sdk.kassa.payments.model.SBPConfirmationDetails
import ru.yoomoney.sdk.kassa.payments.model.PaymentDetails
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.mapper.toPaymentDetails
import java.net.URLDecoder

internal class SBPConfirmationInfoGateway(
    private val paymentsApi: PaymentsApi,
) : SBPConfirmationGateway {

    override suspend fun getConfirmationDetails(
        confirmationData: String,
    ): Result<SBPConfirmationDetails> {
        val uri = withContext(Dispatchers.IO) {
            URLDecoder.decode(confirmationData, Charsets.UTF_8.name()) }
        return paymentsApi.getConfirmationDetails(uri).fold(
            onSuccess = {
                if (it.confirmationData !is ConfirmationDataResponse.SbpConfirmationDataResponse) {
                    Result.Fail(ClassCastException())
                } else {
                    Result.Success(SBPConfirmationDetails(it.paymentId, it.confirmationData.url))
                }
            }, onFailure = {
                Result.Fail(it)
            }
        )
    }

    override suspend fun getPaymentDetails(
        paymentId: String,
    ): Result<PaymentDetails> {
        return paymentsApi.getPaymentDetails(paymentId).fold(
            onSuccess = {
                Result.Success(it.toPaymentDetails())
            }, onFailure = {
                Result.Fail(it)
            }
        )
    }
}