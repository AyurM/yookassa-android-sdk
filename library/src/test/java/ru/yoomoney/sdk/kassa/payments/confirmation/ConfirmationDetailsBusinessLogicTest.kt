/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.confirmation

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl.SBPConfirmationBusinessLogic
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract.Action
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract.State
import ru.yoomoney.sdk.kassa.payments.model.PaymentStatus
import ru.yoomoney.sdk.kassa.payments.model.UserPaymentProcess
import ru.yoomoney.sdk.march.generateBusinessLogicTests

@RunWith(Parameterized::class)
internal class ConfirmationDetailsBusinessLogicTest(
    @Suppress("unused") val testName: String,
    val state: State,
    val action: Action,
    val expected: State
) {

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val failure = Throwable()
            val loadingState = State.Loading
            val loadingDataFailed = State.LoadingDataFailed(failure)
            val confirmationUrl = "url"

            val paymentId = "paymentId"
            val getConfirmationDetailsAction = Action.GetConfirmationDetails("confirmationData")
            val getConfirmationDetailsSuccessAction = Action.GetConfirmationDetailsSuccess(
                paymentId, confirmationUrl
            )
            val getConfirmationDetailsFailedAction = Action.GetConfirmationDetailsFailed(failure)

            val getPaymentDetailsSuccessAction = Action.GetPaymentDetailsSuccess(
                confirmationUrl, paymentId, PaymentStatus.SUCCEEDED, UserPaymentProcess.IN_PROGRESS
            )
            val getPaymentDetailsFailedAction = Action.GetPaymentDetailsFailed(failure)

            return generateBusinessLogicTests<State, Action>(
                generateState = {
                    when (it) {
                        State.Loading::class -> loadingState
                        State.LoadingDataFailed::class -> loadingDataFailed
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        Action.GetConfirmationDetails::class -> getConfirmationDetailsAction
                        Action.GetConfirmationDetailsSuccess::class -> getConfirmationDetailsSuccessAction
                        Action.GetConfirmationDetailsFailed::class -> getConfirmationDetailsFailedAction
                        Action.GetPaymentDetailsSuccess::class -> getPaymentDetailsSuccessAction
                        Action.GetPaymentDetailsFailed::class -> getPaymentDetailsFailedAction
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        loadingState to getConfirmationDetailsAction -> loadingState
                        loadingState to getConfirmationDetailsFailedAction -> loadingDataFailed
                        loadingState to getPaymentDetailsFailedAction -> loadingDataFailed
                        loadingDataFailed to getConfirmationDetailsAction -> loadingState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = SBPConfirmationBusinessLogic(mock(), mock(), mock(), mock())

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        assertThat(actual.state, equalTo(expected))
    }
}