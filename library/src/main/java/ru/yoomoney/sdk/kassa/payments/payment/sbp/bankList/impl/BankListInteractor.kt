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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl

import kotlinx.coroutines.delay
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.UserPaymentProcess
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain

internal interface BankListInteractor {
    suspend fun getSbpWidgetBanks(paymentId: String): BankList.Action

    suspend fun getPaymentStatus(paymentId: String): BankList.Action

    fun searchBank(searchText: String, banks: List<SbpWidgetBankDomain>): List<SbpWidgetBankDomain>

    var bankWasSelected: Boolean
}

internal class BankListInteractorImpl(
    private val bankListRepository: BankListRepository,
) : BankListInteractor {

    override var bankWasSelected: Boolean = false

    override suspend fun getSbpWidgetBanks(paymentId: String): BankList.Action {
        return bankListRepository.getSbpWidgetBanks(paymentId).fold(
            onSuccess = { bankList ->
                BankList.Action.LoadBankListSuccess(bankList)
            },
            onFailure = { BankList.Action.LoadBankListFailed(it) }
        )
    }

    override suspend fun getPaymentStatus(paymentId: String): BankList.Action {
        return bankListRepository.getPaymentStatus(paymentId).fold(
            onSuccess = { response ->
                return when (response.userPaymentProcess) {
                    UserPaymentProcess.FINISHED -> BankList.Action.PaymentProcessFinished
                    else -> BankList.Action.PaymentProcessInProgress
                }
            },
            onFailure = { throwable ->
                if (throwable is ApiMethodException && throwable.error.retryAfter != null) {
                    delay(requireNotNull(throwable.error.retryAfter).toLong())
                    getPaymentStatus(paymentId)
                } else {
                    BankList.Action.PaymentStatusError(throwable)
                }
            }
        )
    }

    override fun searchBank(searchText: String, banks: List<SbpWidgetBankDomain>): List<SbpWidgetBankDomain> {
        return banks.filter { bank ->
            bank.name.contains(searchText, ignoreCase = true)
        }
    }
}
