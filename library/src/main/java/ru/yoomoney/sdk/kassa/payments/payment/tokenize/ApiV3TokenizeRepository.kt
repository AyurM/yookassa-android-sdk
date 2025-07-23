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

package ru.yoomoney.sdk.kassa.payments.payment.tokenize

import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentInstrumentId
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentMethodData
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentMethodId
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensResponse
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.http.createBearerHeader
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.PaymentTokenInfo
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.SBP
import ru.yoomoney.sdk.kassa.payments.model.SberBank
import ru.yoomoney.sdk.kassa.payments.model.YooMoney
import ru.yoomoney.sdk.kassa.payments.model.mapper.toPaymentTokenInfo
import ru.yoomoney.sdk.kassa.payments.model.mapper.toRequest
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.yooprofiler.ProfileEventType
import ru.yoomoney.sdk.yooprofiler.YooProfiler

internal class ApiV3TokenizeRepository(
    private val profilingSessionIdStorage: ProfilingSessionIdStorage,
    private val profiler: YooProfiler,
    private val paymentsApi: PaymentsApi,
    private val merchantCustomerId: String?,
    private val paymentAuthTokenRepository: PaymentAuthTokenRepository
) : TokenizeRepository {

    override suspend fun getToken(
        amount: Amount,
        paymentOption: PaymentOption,
        paymentOptionInfo: PaymentOptionInfo,
        savePaymentMethod: Boolean,
        savePaymentInstrument: Boolean,
        confirmation: Confirmation,
    ): Result<PaymentTokenInfo> {
        val currentProfilingSessionId = acquireProfilingSessionId(paymentOption)
        val paymentAuthToken = paymentAuthTokenRepository.paymentAuthToken
        val tokenRequest = TokensRequestPaymentMethodData(
            amount = amount.toRequest(),
            tmxSessionId = currentProfilingSessionId,
            confirmation = confirmation.toRequest(),
            savePaymentMethod = savePaymentMethod,
            paymentMethodData = paymentOptionInfo.toRequest(paymentOption),
            savePaymentInstrument = if (paymentOption is BankCardPaymentOption) savePaymentInstrument else null,
            merchantCustomerId = if (paymentOption is BankCardPaymentOption) merchantCustomerId else null
        )
        val paymentTokenInfo = paymentsApi.getTokens(
            walletToken = paymentAuthToken?.createBearerHeader(),
            tokensRequest = tokenRequest
        ).fold(
            onSuccess = { handleSuccessTokensRequest(it) },
            onFailure = { Result.Fail(it) }
        )
        profilingSessionIdStorage.profilingSessionId = null
        return paymentTokenInfo
    }

    override suspend fun getToken(
        amount: Amount,
        paymentOption: PaymentOption,
        savePaymentMethod: Boolean,
        confirmation: Confirmation,
        paymentMethodId: String,
        csc: String?,
    ): Result<PaymentTokenInfo> {
        val currentProfilingSessionId = acquireProfilingSessionId(paymentOption)
        val paymentAuthToken = paymentAuthTokenRepository.paymentAuthToken
        val tokenRequest = TokensRequestPaymentMethodId(
            amount = amount.toRequest(),
            tmxSessionId = currentProfilingSessionId,
            confirmation = confirmation.toRequest(),
            savePaymentMethod = savePaymentMethod,
            paymentMethodId = paymentMethodId,
            csc = csc
        )
        val paymentTokenInfo = paymentsApi.getTokens(
            walletToken = paymentAuthToken?.createBearerHeader(),
            tokensRequest = tokenRequest
        ).fold(
            onSuccess = { handleSuccessTokensRequest(it) },
            onFailure = { Result.Fail(it) }
        )
        profilingSessionIdStorage.profilingSessionId = null
        return paymentTokenInfo
    }

    override suspend fun getToken(
        paymentOption: PaymentOption,
        instrumentBankCard: PaymentInstrumentBankCard,
        amount: Amount,
        savePaymentMethod: Boolean,
        csc: String?,
        confirmation: Confirmation,
    ): Result<PaymentTokenInfo> {
        val currentProfilingSessionId = acquireProfilingSessionId(paymentOption)
        val paymentAuthToken = paymentAuthTokenRepository.paymentAuthToken
        val tokenRequest = TokensRequestPaymentInstrumentId(
            amount = amount.toRequest(),
            tmxSessionId = currentProfilingSessionId,
            confirmation = confirmation.toRequest(),
            savePaymentMethod = savePaymentMethod,
            paymentInstrumentId = instrumentBankCard.paymentInstrumentId,
            csc = csc
        )
        val paymentTokenInfo = paymentsApi.getTokens(
            walletToken = paymentAuthToken?.createBearerHeader(),
            tokensRequest = tokenRequest
        ).fold(
            onSuccess = { handleSuccessTokensRequest(it) },
            onFailure = { Result.Fail(it) }
        )
        profilingSessionIdStorage.profilingSessionId = null
        return paymentTokenInfo
    }

    private fun handleSuccessTokensRequest(
        tokensResponse: TokensResponse
    ): Result<PaymentTokenInfo> {
        return Result.Success(tokensResponse.toPaymentTokenInfo())
    }

    private fun acquireProfilingSessionId(paymentOption: PaymentOption): String {
        val profilingSessionId: String?
        val eventType: ProfileEventType
        when(paymentOption) {
            is YooMoney -> {
                profilingSessionId = profilingSessionIdStorage.profilingSessionId
                eventType = ProfileEventType.LOGIN
            }
            is SberBank, is SBP -> {
                return "profilingSessionId"
            }
            else -> {
                profilingSessionId = null
                eventType = ProfileEventType.PAYMENT
            }
        }
        if (profilingSessionId.isNullOrEmpty()) {
            return when (val result = profiler.profile(eventType)) {
                is YooProfiler.Result.Success -> {
                profilingSessionIdStorage.profilingSessionId = result.sessionId
                result.sessionId
            }
                is YooProfiler.Result.Fail -> result.description
            }
        }
        return profilingSessionId
    }
}
