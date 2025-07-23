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

package ru.yoomoney.sdk.kassa.payments.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.HttpException
import retrofit2.Response
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.mapper.map

internal class ConfigApiErrorMapper(
    private val objectMapper: ObjectMapper,
    private val fallbackApiErrorMapper: ApiErrorMapper
): ApiErrorMapper {

    override fun map(response: Response<Any>): Exception {
        return when {
            response.errorBody() !=  null -> parseErrorBody(response)
            else -> fallbackApiErrorMapper.map(response)
        }
    }

    private fun parseErrorBody(response: Response<Any>): Exception {
        return try {
            val json = objectMapper.readTree(response.errorBody()?.string())
            if (json["error"] != null) {
                val error = objectMapper.treeToValue(json["error"], ArgumentsError::class.java).map()
                ApiMethodException(error)
            } else {
                HttpException(response)
            }
        } catch (e: Exception) {
            e
        }
    }
}