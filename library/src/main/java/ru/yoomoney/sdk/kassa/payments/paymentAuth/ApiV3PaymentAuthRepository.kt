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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import android.os.Build
import ru.yoomoney.sdk.kassa.payments.api.PaymentsAuthApi
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthCheckRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthContextGetRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthSessionGenerateRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueExecuteRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueInitRequest
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.map
import ru.yoomoney.sdk.kassa.payments.model.mapper.toAccessTokenModel
import ru.yoomoney.sdk.kassa.payments.model.mapper.toAuthTypeStateModel
import ru.yoomoney.sdk.kassa.payments.model.mapper.toCheckoutTokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.model.mapper.toPairAuthTypes
import ru.yoomoney.sdk.kassa.payments.model.mapper.toPaymentUsageLimit
import ru.yoomoney.sdk.kassa.payments.model.mapper.toProcessPaymentAuthModel
import ru.yoomoney.sdk.kassa.payments.model.mapper.toRequest
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.yooprofiler.ProfileEventType
import ru.yoomoney.sdk.yooprofiler.YooProfiler

internal class ApiV3PaymentAuthRepository(
    private val profilingSessionIdStorage: ProfilingSessionIdStorage,
    private val profiler: YooProfiler,
    private val selectAppropriateAuthType: (AuthType, Array<AuthTypeState>) -> AuthTypeState,
    private val paymentsAuthApi: PaymentsAuthApi
) : PaymentAuthTypeRepository, ProcessPaymentAuthRepository, SmsSessionRetryRepository {

    private var processId: String? = null
    private var authContextId: String? = null
    private var authType: AuthType = AuthType.UNKNOWN

    override suspend fun getPaymentAuthToken(
        currentUser: CurrentUser,
        passphrase: String,
    ): Result<ProcessPaymentAuthGatewayResponse> {
        val currentProcessId: String = processId ?: return Result.Fail(IllegalStateException())

        return when (val result = authCheck(passphrase)) {
            is Result.Success -> getPaymentAuthToken(currentProcessId)
            is Result.Fail -> when (result.value) {
                is AuthCheckApiMethodException -> when (result.value.error.errorCode) {
                    ErrorCode.INVALID_ANSWER -> Result.Success(PaymentAuthWrongAnswer(result.value.authState!!))
                    else -> result
                }
                else -> result
            }
        }
    }

    override suspend fun getPaymentAuthToken(currentUser: CurrentUser): Result<ProcessPaymentAuthGatewayResponse> {
        val currentProcessId: String = processId ?: return Result.Fail(IllegalStateException())
        return getPaymentAuthToken(currentProcessId)
    }

    private suspend fun authCheck(passphrase: String): Result<Unit> {
        val currentAuthType = authType.also {
            check(authType != AuthType.UNKNOWN)
        }

        val currentAuthContextId = authContextId ?: return Result.Fail(IllegalStateException())

        val request = AuthCheckRequest(
            answer = passphrase,
            authType = currentAuthType.toRequest(),
            authContextId = currentAuthContextId,
        )

        return paymentsAuthApi.getAuthCheck(request).fold(
            onSuccess = { response -> response.toProcessPaymentAuthModel() },
            onFailure = { Result.Fail(it) }
        )
    }

    private suspend fun tokenIssueExecute(currentProcessId: String): Result<String> {
        val request = TokenIssueExecuteRequest(currentProcessId)
        return paymentsAuthApi.getTokenIssueExecute(request).fold(
            onSuccess = { response -> response.toAccessTokenModel() },
            onFailure = { Result.Fail(it) }
        )
    }

    private suspend fun getPaymentAuthToken(currentProcessId: String): Result<PaymentAuthToken> {
        return when (val result = tokenIssueExecute(currentProcessId)) {
            is Result.Success -> Result.Success(PaymentAuthToken(result.value))
            is Result.Fail -> result
        }
    }

    override suspend fun getPaymentAuthType(linkWalletToApp: Boolean, amount: Amount): Result<AuthTypeState> {
        processId = null
        authContextId = null
        authType = AuthType.UNKNOWN

        return when (val result = tokenIssueInit(amount, linkWalletToApp)) {
            is Result.Success -> parseSuccessToAuthTypeState(result)
            is Result.Fail -> result
        }
    }

    private suspend fun parseSuccessToAuthTypeState(result: Result.Success<CheckoutTokenIssueInitResponse>) =
        when (result.value) {
            is CheckoutTokenIssueInitResponse.Success -> {
                this.processId = result.value.processId
                Result.Success(AuthTypeState.NotRequired)
            }
            is CheckoutTokenIssueInitResponse.AuthRequired -> handleAuthRequired(result.value)
        }

    private suspend fun handleAuthRequired(
        tokenIssueInitResponse: CheckoutTokenIssueInitResponse.AuthRequired,
    ): Result<AuthTypeState> {
        this.processId = tokenIssueInitResponse.processId
        this.authContextId = tokenIssueInitResponse.authContextId

        val localAuthContextId: String = authContextId ?: return Result.Fail(IllegalStateException())
        val authTypeState = when (val result = getAuthContextGet(localAuthContextId)) {
            is Result.Success -> result.value
            is Result.Fail -> return result
        }
        authType = authTypeState.type

        val updatedAuthTypeState = when (val result = authSessionGenerate()) {
            is Result.Success -> result.value
            else -> return result
        }
        authType = updatedAuthTypeState.type

        return Result.Success(updatedAuthTypeState)
    }

    private suspend fun tokenIssueInit(
        amount: Amount,
        multipleUsage: Boolean,
    ): Result<CheckoutTokenIssueInitResponse> {
        val request = TokenIssueInitRequest(
            instanceName = Build.MANUFACTURER + ", " + Build.MODEL,
            singleAmountMax = if (multipleUsage.not()) amount.toRequest() else null,
            paymentUsageLimit = multipleUsage.toPaymentUsageLimit(),
            tmxSessionId = loadProfilingSession(),
        )
        return paymentsAuthApi.getTokenIssueInit(request).fold(
            onSuccess = { response -> response.toCheckoutTokenIssueInitResponse() },
            onFailure = { Result.Fail(it) }
        )
    }

    private fun loadProfilingSession(): String {
        var profilingSessionId = profilingSessionIdStorage.profilingSessionId
        if (profilingSessionId.isNullOrEmpty()) {
            profilingSessionId =
                when (val result = profiler.profile(ProfileEventType.LOGIN)) {
                    is YooProfiler.Result.Success -> result.sessionId
                    is YooProfiler.Result.Fail -> result.description
                }
        }
        return profilingSessionId
    }

    private suspend fun getAuthContextGet(localAuthContextId: String): Result<AuthTypeState> {
        val request = AuthContextGetRequest(localAuthContextId)
        return paymentsAuthApi.getAuthContext(request).fold(
            onSuccess = { response ->
                response.toPairAuthTypes().map {
                    selectAppropriateAuthType(it.defaultAuthType, it.authTypeStates)
                }
            },
            onFailure = { Result.Fail(it) }
        )
    }

    private suspend fun authSessionGenerate(): Result<AuthTypeState> {
        val currentAuthType = authType.takeIf { it != AuthType.UNKNOWN } ?: return Result.Fail(IllegalStateException())
        val currentAuthContextId = authContextId ?: return Result.Fail(IllegalStateException())
        val request = AuthSessionGenerateRequest(
            authContextId = currentAuthContextId,
            authType = currentAuthType.toRequest(),
        )
        return paymentsAuthApi.getAuthSessionGenerate(request).fold(
            onSuccess = { response -> response.toAuthTypeStateModel() },
            onFailure = { Result.Fail(it) }
        )
    }

    override suspend fun retrySmsSession(): Result<AuthTypeState> {
        return authSessionGenerate()
    }
}