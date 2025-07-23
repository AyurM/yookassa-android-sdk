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

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import ru.yoomoney.sdk.kassa.payments.api.failures.ApiErrorMapper
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class SuspendResultCallAdapterFactory(
    private val apiErrorMapper: ApiErrorMapper
) : CallAdapter.Factory() {

    private var hasConverterForResult: Boolean? = null
    private fun Retrofit.hasConverterForResultType(resultType: Type): Boolean {
        // If converter exists for any `Result<T>`,
        // user registered custom converter for `Result` type.
        // No need to check again.
        if (hasConverterForResult == true) return true
        return runCatching {
            nextResponseBodyConverter<Result<*>>(null, resultType, arrayOf())
        }.isSuccess.also { hasConverterForResult = it }
    }

    /**
     * Represents Type `Call<T>`, where `T` is passed in [dataType]
     */
    private class CallDataType(
        private val dataType: Type
    ) : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(dataType)
        override fun getRawType(): Type = Call::class.java
        override fun getOwnerType(): Type? = null
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        // suspend function is represented by `Call<Result<T>>`
        if (getRawType(returnType) != Call::class.java) return null
        if (returnType !is ParameterizedType) return null

        // Result<T>
        val resultType: Type = getParameterUpperBound(0, returnType)
        if (getRawType(resultType) != Result::class.java
            || resultType !is ParameterizedType
        ) return null

        val dataType = getParameterUpperBound(0, resultType)

        val delegateType = if (retrofit.hasConverterForResultType(resultType))
            returnType else CallDataType(dataType)

        val delegate: CallAdapter<*, *> = retrofit
            .nextCallAdapter(this, delegateType, annotations)

        return CatchingCallAdapter(delegate, apiErrorMapper)
    }

    private class CatchingCallAdapter(
        private val delegate: CallAdapter<*, *>,
        private val apiErrorMapper: ApiErrorMapper
    ) : CallAdapter<Any, Call<Result<*>>> {
        override fun responseType(): Type = delegate.responseType()
        override fun adapt(call: Call<Any>): Call<Result<*>> = CatchingCall(call, apiErrorMapper)
    }

    private class CatchingCall(
        private val delegate: Call<Any>,
        private val apiErrorMapper: ApiErrorMapper
    ) : Call<Result<*>> {

        override fun enqueue(callback: Callback<Result<*>>) = delegate.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    callback.onResponse(
                        this@CatchingCall,
                        Response.success(Result.success(body))
                    )
                } else {
                    val exception = apiErrorMapper.map(response)
                    callback.onResponse(
                        this@CatchingCall,
                        Response.success(Result.failure<Any>(exception))
                    )
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onResponse(
                    this@CatchingCall,
                    Response.success(Result.failure<Any>(t))
                )
            }
        })

        override fun clone(): Call<Result<*>> = CatchingCall(delegate, apiErrorMapper)
        override fun execute(): Response<Result<*>> =
            throw UnsupportedOperationException("Suspend function should not be blocking.")

        override fun isExecuted(): Boolean = delegate.isExecuted
        override fun cancel(): Unit = delegate.cancel()
        override fun isCanceled(): Boolean = delegate.isCanceled
        override fun request(): Request = delegate.request()
        override fun timeout(): Timeout = delegate.timeout()
    }
}