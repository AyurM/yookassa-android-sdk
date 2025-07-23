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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.yoomoney.sdk.kassa.payments.api.model.confirmationdata.ConfirmationDetailsResponse
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionsResponse
import ru.yoomoney.sdk.kassa.payments.api.model.paymentdetails.PaymentDetailsResponse
import ru.yoomoney.sdk.kassa.payments.api.model.paymentmethod.PaymentMethodResponse
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentInstrumentId
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentMethodData
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensRequestPaymentMethodId
import ru.yoomoney.sdk.kassa.payments.api.model.tokens.TokensResponse

internal interface PaymentsApi {

    @GET("/api/frontend/v3/payment_options")
    suspend fun getPaymentOptions(
        @Query("gateway_id") gatewayId: String?,
        @Query("amount") amount: String?,
        @Query("currency") currency: String?,
        @Query("save_payment_method") savePaymentMethod: Boolean?,
        @Query("merchant_customer_id") merchantCustomerId: String?
    ): Result<PaymentOptionsResponse>

    @GET("/api/frontend/v3/payment_method")
    suspend fun getPaymentMethod(
        @Query("payment_method_id") paymentMethodId: String,
    ): Result<PaymentMethodResponse>

    @POST("/api/frontend/v3/tokens")
    suspend fun getTokens(
        @Header("Wallet-Authorization") walletToken: String?,
        @Body tokensRequest: TokensRequestPaymentMethodData
    ): Result<TokensResponse>

    @POST("/api/frontend/v3/tokens")
    suspend fun getTokens(
        @Header("Wallet-Authorization") walletToken: String?,
        @Body tokensRequest: TokensRequestPaymentInstrumentId
    ): Result<TokensResponse>

    @POST("/api/frontend/v3/tokens")
    suspend fun getTokens(
        @Header("Wallet-Authorization") walletToken: String?,
        @Body tokensRequest: TokensRequestPaymentMethodId
    ): Result<TokensResponse>

    @DELETE("/api/frontend/v3/payment_instruments/{payment_instrument_id}")
    suspend fun deletePaymentInstrumentId(
        @Path("payment_instrument_id") instrumentId: String
    ): Result<Unit>

    @GET("/api/frontend/v3/confirmation_details")
    suspend fun getConfirmationDetails(
        @Query("confirmation_data") confirmationData: String
    ): Result<ConfirmationDetailsResponse>

    @GET("/api/frontend/v3/payments/{payment_id}")
    suspend fun getPaymentDetails(
        @Path("payment_id") paymentId: String
    ): Result<PaymentDetailsResponse>
}