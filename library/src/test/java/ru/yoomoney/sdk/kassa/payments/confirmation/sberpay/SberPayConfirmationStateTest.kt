/*
 * The MIT License (MIT)
 * Copyright © 2024 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.confirmation.sberpay

import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.march.generateBusinessLogicTests


@RunWith(Parameterized::class)
internal class SberPayConfirmationStateTest(
    @Suppress("unused") val testName: String,
    val state: SberPayConfirmationContract.State,
    val action: SberPayConfirmationContract.Action,
    val expected: SberPayConfirmationContract.State,
) {
    private val confirmationData = "confirmationData"
    private val shopId = "shopId"

    private val logic = SberPayConfirmationBusinessLogic(mock(), mock(), mock(), mock(), confirmationData, shopId)

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertThat(actual.state, CoreMatchers.equalTo(expected))
    }

    companion object {
        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val throwable = Throwable()
            val loadingState = SberPayConfirmationContract.State.Loading
            val failedState = SberPayConfirmationContract.State.LoadSberPayInfoFailed(throwable)
            val loadSberInfoAction = SberPayConfirmationContract.Action.LoadSberPayInfo
            val finishedStatusAction = SberPayConfirmationContract.Action.FinishedPaymentStatus
            val loadSberInfoSuccessAction = SberPayConfirmationContract.Action.LoadSberPayInfoSuccess(
                "sberApikey",
                "merchantLogin",
                "orderId",
                "orderNumber"
            )
            val loadSberInfoFailedAction = SberPayConfirmationContract.Action.LoadSberPayInfoFailed(throwable)
            return generateBusinessLogicTests<SberPayConfirmationContract.State, SberPayConfirmationContract.Action>(
                generateState = {
                    when (it) {
                        SberPayConfirmationContract.State.Loading::class -> loadingState
                        SberPayConfirmationContract.State.LoadSberPayInfoFailed::class -> failedState
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateAction = {
                    when (it) {
                        SberPayConfirmationContract.Action.LoadSberPayInfo::class -> loadSberInfoAction
                        SberPayConfirmationContract.Action.FinishedPaymentStatus::class -> finishedStatusAction
                        SberPayConfirmationContract.Action.LoadSberPayInfoSuccess::class -> loadSberInfoSuccessAction
                        SberPayConfirmationContract.Action.LoadSberPayInfoFailed::class -> loadSberInfoFailedAction
                        else -> it.objectInstance ?: error(it)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        loadingState to finishedStatusAction -> loadingState
                        loadingState to loadSberInfoSuccessAction -> loadingState
                        loadingState to loadSberInfoFailedAction -> failedState
                        failedState to loadSberInfoAction -> loadingState
                        else -> state
                    }
                }
            )
        }
    }
}