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

import ru.yoomoney.sdk.kassa.payments.api.ErrorResponse
import ru.yoomoney.sdk.kassa.payments.api.config.ArgumentsError
import ru.yoomoney.sdk.kassa.payments.model.Error
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode

internal fun ErrorResponse.map() = Error(
    id = id,
    errorCode = code.map(),
    description = description,
    parameter = parameter,
    retryAfter = retryAfter?.toInt()
)

internal fun ArgumentsError.map(): Error = when (this) {
    is ArgumentsError.ArgumentsSyntaxError -> Error(ErrorCode.SYNTAX_ERROR)
    is ArgumentsError.ArgumentsParametersError -> Error(
        errorCode = ErrorCode.ILLEGAL_PARAMETERS,
        parameter = parameterNames.firstOrNull()
    )
    is ArgumentsError.ArgumentsHeadersError -> Error(
        errorCode = ErrorCode.ILLEGAL_HEADERS,
        parameter = headerNames.firstOrNull()
    )
    is ArgumentsError.UnknownArgumentsError -> Error(
        errorCode = ErrorCode.UNKNOWN
    )
}