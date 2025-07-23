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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import ru.yoomoney.sdk.kassa.payments.model.AuthTypeState

internal sealed interface PaymentAuthUiState {
    object Loading : PaymentAuthUiState

    data class StartError(val error: Throwable) : PaymentAuthUiState

    data class InputCode(val data: AuthTypeState.SMS) : PaymentAuthUiState

    data class InputCodeProcess(val passphrase: String, val data: AuthTypeState.SMS) : PaymentAuthUiState

    data class WrongInputCode(
        val data: AuthTypeState.SMS,
        val attemptsCount: Int?,
        val attemptsLeft: Int?,
    ) : PaymentAuthUiState

    data class InputCodeVerifyExceeded(val passphrase: String, val data: AuthTypeState.SMS) : PaymentAuthUiState

    data class ProcessError(val data: AuthTypeState.SMS, val error: Throwable) : PaymentAuthUiState
}

internal fun PaymentAuth.State.mapToUiState(): PaymentAuthUiState = when (this) {
    is PaymentAuth.State.Loading -> PaymentAuthUiState.Loading
    is PaymentAuth.State.StartError -> PaymentAuthUiState.StartError(this.error)
    is PaymentAuth.State.InputCode -> PaymentAuthUiState.InputCode(this.data)
    is PaymentAuth.State.InputCodeProcess -> PaymentAuthUiState.InputCodeProcess(this.passphrase, this.data)
    is PaymentAuth.State.InputCodeVerifyExceeded -> PaymentAuthUiState.InputCodeVerifyExceeded(
        this.passphrase,
        this.data
    )

    is PaymentAuth.State.ProcessError -> PaymentAuthUiState.ProcessError(this.data, this.error)
    is PaymentAuth.State.WrongInputCode -> PaymentAuthUiState.WrongInputCode(
        this.data,
        this.attemptsCount,
        this.attemptsLeft
    )
}