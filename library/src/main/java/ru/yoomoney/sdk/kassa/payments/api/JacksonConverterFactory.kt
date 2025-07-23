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

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.ResultJacksonResponseBodyConverter
import java.lang.reflect.Type

internal class YooKassaJacksonConverterFactory private constructor(
    private val mapper: ObjectMapper
) : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> {
        return ResultJacksonResponseBodyConverter.responseBodyConverter<Any>(type, mapper)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return ResultJacksonResponseBodyConverter.requestBodyConverter<Any>(type, mapper)
    }

    companion object {
        /** Create an instance using `mapper` for conversion.  */
        /** Create an instance using a default [ObjectMapper] instance for conversion.  */
        @JvmOverloads  // Guarding public API nullability.
        fun create(mapper: ObjectMapper? = ObjectMapper()): YooKassaJacksonConverterFactory {
            if (mapper == null) throw NullPointerException("mapper == null")
            return YooKassaJacksonConverterFactory(mapper)
        }
    }
}