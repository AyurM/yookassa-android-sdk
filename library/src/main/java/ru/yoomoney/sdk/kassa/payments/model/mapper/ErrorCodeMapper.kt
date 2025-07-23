/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

import ru.yoomoney.sdk.kassa.payments.api.ErrorResponseCode
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode

internal fun ErrorResponseCode.map() = when(this) {
    ErrorResponseCode.INVALID_REQUEST -> ErrorCode.INVALID_REQUEST
    ErrorResponseCode.NOT_SUPPORTED -> ErrorCode.NOT_SUPPORTED
    ErrorResponseCode.INVALID_CREDENTIALS -> ErrorCode.INVALID_CREDENTIALS
    ErrorResponseCode.FORBIDDEN -> ErrorCode.FORBIDDEN
    ErrorResponseCode.INTERNAL_SERVER_ERROR -> ErrorCode.INTERNAL_SERVER_ERROR
    ErrorResponseCode.TECHNICAL_ERROR -> ErrorCode.TECHNICAL_ERROR
    ErrorResponseCode.INVALID_SCOPE -> ErrorCode.INVALID_SCOPE
    ErrorResponseCode.INVALID_LOGIN -> ErrorCode.INVALID_LOGIN
    ErrorResponseCode.INVALID_TOKEN -> ErrorCode.INVALID_TOKEN
    ErrorResponseCode.INVALID_SIGNATURE -> ErrorCode.INVALID_SIGNATURE
    ErrorResponseCode.SYNTAX_ERROR -> ErrorCode.SYNTAX_ERROR
    ErrorResponseCode.ILLEGAL_PARAMETERS -> ErrorCode.ILLEGAL_PARAMETERS
    ErrorResponseCode.ILLEGAL_HEADERS -> ErrorCode.ILLEGAL_HEADERS
    ErrorResponseCode.INVALID_CONTEXT -> ErrorCode.INVALID_CONTEXT
    ErrorResponseCode.CREATE_TIMEOUT_NOT_EXPIRED -> ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED
    ErrorResponseCode.SESSIONS_EXCEEDED -> ErrorCode.SESSIONS_EXCEEDED
    ErrorResponseCode.UNSUPPORTED_AUTH_TYPE -> ErrorCode.UNSUPPORTED_AUTH_TYPE
    ErrorResponseCode.VERIFY_ATTEMPTS_EXCEEDED -> ErrorCode.VERIFY_ATTEMPTS_EXCEEDED
    ErrorResponseCode.INVALID_ANSWER -> ErrorCode.INVALID_ANSWER
    ErrorResponseCode.SESSION_DOES_NOT_EXIST -> ErrorCode.SESSION_DOES_NOT_EXIST
    ErrorResponseCode.SESSION_EXPIRED -> ErrorCode.SESSION_EXPIRED
    ErrorResponseCode.ACCOUNT_NOT_FOUND -> ErrorCode.ACCOUNT_NOT_FOUND
    ErrorResponseCode.AUTH_REQUIRED -> ErrorCode.AUTH_REQUIRED
    ErrorResponseCode.AUTH_EXPIRED -> ErrorCode.AUTH_EXPIRED
    ErrorResponseCode.UNKNOWN -> ErrorCode.UNKNOWN
}