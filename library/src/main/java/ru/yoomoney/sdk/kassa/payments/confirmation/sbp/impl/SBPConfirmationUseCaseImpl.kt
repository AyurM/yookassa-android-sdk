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

import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract
import ru.yoomoney.sdk.kassa.payments.model.Result

internal class SBPConfirmationUseCaseImpl(
    private val paymentDetailsGateway: SBPConfirmationGateway,
) : SBPConfirmationUseCase {

    override suspend fun getConfirmationDetails(confirmationData: String): SBPConfirmationContract.Action {
        return when (val confirmationDetails = paymentDetailsGateway.getConfirmationDetails(confirmationData)) {
            is Result.Success -> {
                SBPConfirmationContract.Action.GetConfirmationDetailsSuccess(
                    confirmationDetails.value.paymentId,
                    confirmationDetails.value.url
                )
            }
            is Result.Fail -> SBPConfirmationContract.Action.GetConfirmationDetailsFailed(confirmationDetails.value)
        }
    }

    override suspend fun getPaymentDetails(paymentId: String, confirmationUrl: String): SBPConfirmationContract.Action {
        return when (val paymentDetails = paymentDetailsGateway.getPaymentDetails(paymentId)) {
            is Result.Success -> {
                SBPConfirmationContract.Action.GetPaymentDetailsSuccess(
                    confirmationUrl,
                    paymentDetails.value.paymentId,
                    paymentDetails.value.status,
                    paymentDetails.value.userPaymentProcess
                )
            }
            is Result.Fail -> {
                SBPConfirmationContract.Action.GetPaymentDetailsFailed(paymentDetails.value)
            }
        }
    }
}