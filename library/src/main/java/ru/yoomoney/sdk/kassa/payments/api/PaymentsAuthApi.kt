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

package ru.yoomoney.sdk.kassa.payments.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthCheckRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthCheckResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthContextGetRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthSessionGenerateRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthSessionGenerateResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueExecuteRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueExecuteResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueInitRequest
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueInitResponse

internal interface PaymentsAuthApi {

    @POST("/api/wallet-auth/v1/checkout/auth-check")
    suspend fun getAuthCheck(@Body authCheckRequest: AuthCheckRequest): Result<AuthCheckResponse>

    @POST("/api/wallet-auth/v1/checkout/auth-context-get")
    suspend fun getAuthContext(@Body authCheckRequest: AuthContextGetRequest): Result<AuthContextGetResponse>

    @POST("/api/wallet-auth/v1/checkout/auth-session-generate")
    suspend fun getAuthSessionGenerate(
        @Body authSessionGenerateRequest: AuthSessionGenerateRequest
    ): Result<AuthSessionGenerateResponse>

    @POST("/api/wallet-auth/v1/checkout/token-issue-execute")
    suspend fun getTokenIssueExecute(
        @Body authSessionGenerateRequest: TokenIssueExecuteRequest
    ): Result<TokenIssueExecuteResponse>

    @POST("/api/wallet-auth/v1/checkout/token-issue-init")
    suspend fun getTokenIssueInit(
        @Body authSessionGenerateRequest: TokenIssueInitRequest
    ): Result<TokenIssueInitResponse>

}