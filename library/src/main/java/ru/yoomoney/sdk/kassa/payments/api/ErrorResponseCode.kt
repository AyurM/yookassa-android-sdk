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

package ru.yoomoney.sdk.kassa.payments.api

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty

internal enum class ErrorResponseCode {

    @JsonProperty("invalid_request")
    INVALID_REQUEST,

    @JsonProperty("not_supported")
    NOT_SUPPORTED,

    @JsonProperty("invalid_credentials")
    INVALID_CREDENTIALS,

    @JsonProperty("forbidden")
    FORBIDDEN,

    @JsonProperty("internal_server_error")
    INTERNAL_SERVER_ERROR,

    @JsonProperty("technical_error")
    TECHNICAL_ERROR,

    @JsonProperty("invalid_scope")
    INVALID_SCOPE,

    @JsonProperty("invalid_login")
    INVALID_LOGIN,

    @JsonProperty("invalid_token")
    INVALID_TOKEN,

    @JsonProperty("invalid_signature")
    INVALID_SIGNATURE,

    @JsonProperty("syntax_error")
    SYNTAX_ERROR,

    @JsonProperty("illegal_parameters")
    ILLEGAL_PARAMETERS,

    @JsonProperty("illegal_headers")
    ILLEGAL_HEADERS,

    @JsonProperty("invalid_context")
    INVALID_CONTEXT,

    @JsonProperty("create_timeout_not_expired")
    CREATE_TIMEOUT_NOT_EXPIRED,

    @JsonProperty("sessions_exceeded")
    SESSIONS_EXCEEDED,

    @JsonProperty("unsupported_auth_type")
    UNSUPPORTED_AUTH_TYPE,

    @JsonProperty("verify_attempts_exceeded")
    VERIFY_ATTEMPTS_EXCEEDED,

    @JsonProperty("invalid_answer")
    INVALID_ANSWER,

    @JsonProperty("session_does_not_exist")
    SESSION_DOES_NOT_EXIST,

    @JsonProperty("session_expired")
    SESSION_EXPIRED,

    @JsonProperty("account_not_found")
    ACCOUNT_NOT_FOUND,

    @JsonProperty("auth_required")
    AUTH_REQUIRED,

    @JsonProperty("auth_expired")
    AUTH_EXPIRED,

    @JsonEnumDefaultValue
    UNKNOWN
}