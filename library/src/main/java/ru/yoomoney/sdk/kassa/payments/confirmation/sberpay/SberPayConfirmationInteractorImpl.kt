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

import kotlinx.coroutines.delay
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.UserPaymentProcess

internal class SberPayConfirmationInteractorImpl(
    private val sberPayRepository: SberPayConfirmationRepository
) : SberPayConfirmationInteractor {

    override suspend fun loadSberPayInfo(confirmationData: String, shopId: String): SberPayConfirmationContract.Action {

        val sberConfirmationDetails = when (val details = sberPayRepository.getConfirmationDetails(confirmationData)) {
            is Result.Success -> details.value
            is Result.Fail -> return handleFail(details.value, confirmationData, shopId)
        }

        val paymentDetails = when (val details = sberPayRepository.getPaymentDetails(sberConfirmationDetails.paymentId)) {
            is Result.Success -> details.value
            is Result.Fail -> return handleFail(details.value, confirmationData, shopId)
        }
        if (paymentDetails.userPaymentProcess == UserPaymentProcess.FINISHED) {
            return SberPayConfirmationContract.Action.FinishedPaymentStatus
        }

        return SberPayConfirmationContract.Action.LoadSberPayInfoSuccess(
            sberConfirmationDetails.apiKey,
            sberConfirmationDetails.merchantLogin,
            sberConfirmationDetails.orderId,
            sberConfirmationDetails.orderNumber
        )
    }

    private suspend fun handleFail(
        throwable: Throwable,
        confirmationData: String,
        shopId: String,
    ) = if (throwable is ApiMethodException && throwable.error.retryAfter != null) {
        delay(requireNotNull(throwable.error.retryAfter).toLong())
        loadSberPayInfo(confirmationData, shopId)
    } else {
        SberPayConfirmationContract.Action.LoadSberPayInfoFailed(throwable)
    }
}
