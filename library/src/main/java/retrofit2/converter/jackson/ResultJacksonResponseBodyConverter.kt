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

package retrofit2.converter.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal object ResultJacksonResponseBodyConverter {

    fun <T> responseBodyConverter(type: Type, mapper: ObjectMapper): Converter<ResponseBody, T> {
        return if (type is ParameterizedType && type.rawType == Result::class.java) {
            val resultType = type.actualTypeArguments.firstOrNull()
            val rType = mapper.typeFactory.constructType(resultType)
            val reader = mapper.readerFor(rType)
            JacksonResponseBodyConverter(reader)
        } else {
            val resultType = mapper.typeFactory.constructType(type)
            val reader = mapper.readerFor(resultType)
            JacksonResponseBodyConverter(reader)
        }
    }

    fun <T> requestBodyConverter(type: Type, mapper: ObjectMapper): Converter<T, RequestBody> {
        val javaType = mapper.typeFactory.constructType(type)
        val writer = mapper.writerFor(javaType)
        return JacksonRequestBodyConverter(writer)
    }
}