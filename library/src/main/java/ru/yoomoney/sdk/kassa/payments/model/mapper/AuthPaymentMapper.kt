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

package ru.yoomoney.sdk.kassa.payments.model.mapper

import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthCheckResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthPaymentState
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthSessionGenerateResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthTypeStateResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.AuthTypes
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.ErrorCodeNetwork
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.PaymentUsageLimit
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueExecuteResponse
import ru.yoomoney.sdk.kassa.payments.api.model.authpayments.TokenIssueInitResponse
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthCheckApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.AuthType
import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState
import ru.yoomoney.sdk.kassa.payments.model.Error
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.paymentAuth.CheckoutAuthContextGetResponse
import ru.yoomoney.sdk.kassa.payments.paymentAuth.CheckoutTokenIssueInitResponse

internal fun AuthType.toRequest(): AuthTypes = when (this) {
    AuthType.SMS -> AuthTypes.SMS
    AuthType.EMERGENCY -> AuthTypes.EMERGENCY
    AuthType.TOTP -> AuthTypes.TOTP
    AuthType.SECURE_PASSWORD -> AuthTypes.SECURE_PASSWORD
    AuthType.PUSH -> AuthTypes.PUSH
    AuthType.OAUTH_TOKEN -> AuthTypes.OAUTH_TOKEN
    AuthType.PUSH_CODE -> AuthTypes.PUSH_CODE
    AuthType.UNKNOWN -> AuthTypes.UNKNOWN
}

internal fun AuthTypes.toAuthTypeModel(): AuthType = when (this) {
    AuthTypes.SMS -> AuthType.SMS
    AuthTypes.EMERGENCY -> AuthType.EMERGENCY
    AuthTypes.TOTP -> AuthType.TOTP
    AuthTypes.SECURE_PASSWORD -> AuthType.SECURE_PASSWORD
    AuthTypes.PUSH -> AuthType.PUSH
    AuthTypes.OAUTH_TOKEN -> AuthType.OAUTH_TOKEN
    AuthTypes.PUSH_CODE -> AuthType.PUSH_CODE
    AuthTypes.UNKNOWN -> AuthType.UNKNOWN
}

internal fun AuthCheckResponse.toProcessPaymentAuthModel(): Result<Unit> {
    return when {
        status == AuthPaymentState.SUCCESS -> Result.Success(Unit)
        status == AuthPaymentState.REFUSED && error != null -> Result.Fail(
            AuthCheckApiMethodException(
                Error(error.toErrorCode()),
                result?.toAuthTypeState()
            )
        )
        status == AuthPaymentState.UNKNOWN && error != null -> Result.Fail(
            ApiMethodException(Error(error.toErrorCode()))
        )
        else -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
    }
}

internal fun TokenIssueExecuteResponse.toAccessTokenModel(): Result<String> {
    return when {
        status == AuthPaymentState.SUCCESS && result != null -> Result.Success(result.accessToken)
        error != null -> Result.Fail(ApiMethodException(Error(error.toErrorCode())))
        else -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
    }
}

internal fun AuthContextGetResponse.toPairAuthTypes(): Result<CheckoutAuthContextGetResponse> {
    return when {
        status == AuthPaymentState.SUCCESS -> {
            Result.Success(
                CheckoutAuthContextGetResponse(
                    authTypeStates = result?.authTypes
                        ?.filter { it.enabled == true }
                        ?.map { it.toAuthTypeState() }
                        ?.toTypedArray() ?: emptyArray(),
                    defaultAuthType = result?.defaultAuthType?.toAuthTypeModel() ?: AuthType.UNKNOWN
                )
            )
        }
        status == AuthPaymentState.REFUSED && error != null -> {
            Result.Fail(ApiMethodException(Error(error.toErrorCode())))
        }
        status == AuthPaymentState.UNKNOWN && error != null -> {
            Result.Fail(ApiMethodException(Error(error.toErrorCode())))
        }
        else -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
    }
}

internal fun AuthSessionGenerateResponse.toAuthTypeStateModel(): Result<AuthTypeState> {
    return when {
        status == AuthPaymentState.SUCCESS && result != null -> Result.Success(result.toAuthTypeState())
        status == AuthPaymentState.REFUSED && error != null -> {
            Result.Fail(ApiMethodException(Error(error.toErrorCode())))
        }
        status == AuthPaymentState.UNKNOWN && error != null ->
            Result.Fail(ApiMethodException(Error(error.toErrorCode())))

        else -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
    }
}

internal fun AuthTypeStateResponse.toAuthTypeState(): AuthTypeState {
    return when (type) {
        AuthTypes.UNKNOWN -> AuthTypeState.NotRequired
        AuthTypes.PUSH -> AuthTypeState.Push
        AuthTypes.EMERGENCY -> AuthTypeState.Emergency
        AuthTypes.OAUTH_TOKEN -> AuthTypeState.OauthToken
        AuthTypes.SECURE_PASSWORD -> AuthTypeState.SecurePassword
        AuthTypes.TOTP -> AuthTypeState.TOTP
        AuthTypes.SMS -> AuthTypeState.SMS(
            nextSessionTimeLeft = nextSessionTimeLeft,
            codeLength = codeLength,
            attemptsCount = attemptsCount,
            attemptsLeft = attemptsLeft
        )
        else -> AuthTypeState.UNKNOWN
    }
}

internal fun ErrorCodeNetwork.toErrorCode(): ErrorCode = when (this) {
    ErrorCodeNetwork.INVALID_CONTEXT -> ErrorCode.INVALID_CONTEXT
    ErrorCodeNetwork.UNSUPPORTED_AUTH_TYPE -> ErrorCode.UNSUPPORTED_AUTH_TYPE
    ErrorCodeNetwork.INVALID_ANSWER -> ErrorCode.INVALID_ANSWER
    ErrorCodeNetwork.VERIFY_ATTEMPTS_EXCEEDED -> ErrorCode.VERIFY_ATTEMPTS_EXCEEDED
    ErrorCodeNetwork.SESSION_DOES_NOT_EXIST -> ErrorCode.SESSION_DOES_NOT_EXIST
    ErrorCodeNetwork.SESSION_EXPIRED -> ErrorCode.SESSION_EXPIRED
    ErrorCodeNetwork.UNKNOWN -> ErrorCode.UNKNOWN
}

internal fun Boolean.toPaymentUsageLimit(): PaymentUsageLimit = if (this) {
    PaymentUsageLimit.MULTIPLE
} else {
    PaymentUsageLimit.SINGLE
}

internal fun TokenIssueInitResponse.toCheckoutTokenIssueInitResponse(): Result<CheckoutTokenIssueInitResponse> {
    return when {
        status == AuthPaymentState.SUCCESS && result != null -> {
            Result.Success(CheckoutTokenIssueInitResponse.Success(result.processId))
        }
        status == AuthPaymentState.AUTH_REQUIRED && result != null -> {
            Result.Success(
                CheckoutTokenIssueInitResponse.AuthRequired(
                    authContextId = result.authContextId,
                    processId = result.processId
                )
            )
        }
        status == AuthPaymentState.UNKNOWN && error != null-> {
            Result.Fail(ApiMethodException(Error(error.toErrorCode())))
        }
        else -> Result.Fail(ApiMethodException(ErrorCode.TECHNICAL_ERROR))
    }
}
