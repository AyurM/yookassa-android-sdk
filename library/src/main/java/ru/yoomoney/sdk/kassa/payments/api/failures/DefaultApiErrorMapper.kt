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

package ru.yoomoney.sdk.kassa.payments.api.failures

import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.HttpException
import retrofit2.Response
import ru.yoomoney.sdk.kassa.payments.api.ErrorResponse
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.NotModifiedFailure
import ru.yoomoney.sdk.kassa.payments.model.mapper.map
import java.net.HttpURLConnection

internal class DefaultApiErrorMapper(
    private val objectMapper: ObjectMapper
): ApiErrorMapper {

    override fun map(response: Response<Any>): Exception {
        return when {
            response.code() == HttpURLConnection.HTTP_NOT_MODIFIED -> NotModifiedFailure
            response.errorBody() !=  null -> parseErrorBody(response)
            else -> HttpException(response)
        }
    }

    private fun parseErrorBody(response: Response<Any>): Exception {
        return try {
            val errorResponse = objectMapper.readValue(response.errorBody()?.string(), ErrorResponse::class.java)
            ApiMethodException(errorResponse.map())
        } catch (e: Exception) {
            e
        }
    }
}