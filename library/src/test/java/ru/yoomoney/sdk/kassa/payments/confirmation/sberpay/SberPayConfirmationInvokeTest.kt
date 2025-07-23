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
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.utils.func

internal class SberPayConfirmationInvokeTest {


    private val showState: (SberPayConfirmationContract.State) -> SberPayConfirmationContract.Action = mock()
    private val showEffect: (SberPayConfirmationContract.Effect) -> Unit = mock()
    private val source: () -> SberPayConfirmationContract.Action = mock()
    private val sberPayConfirmationInteractor: SberPayConfirmationInteractor = mock()
    private val confirmationData = "confirmationData"
    private val shopId = "shopId"
    private val error = Throwable("test error")

    private val logic =
        SberPayConfirmationBusinessLogic(showState, showEffect, source, sberPayConfirmationInteractor, confirmationData, shopId)

    @Test
    fun `Should send CloseWithFinishState effect with Loading state and FinishedPaymentStatus action`() {
        val expectedEffect = SberPayConfirmationContract.Effect.CloseWithFinishState
        val out = logic(
            SberPayConfirmationContract.State.Loading,
            SberPayConfirmationContract.Action.FinishedPaymentStatus
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expectedEffect)
    }

    @Test
    fun `Should send ShowSberPaySdk effect with Loading state and LoadSberPayInfoSuccess action`() {
        val expectedEffect = SberPayConfirmationContract.Effect.ShowSberPaySdk(
            "sberPayApiKey",
            "merchantLogin",
            "orderId",
            "orderNumber"
        )
        val out = logic(
            SberPayConfirmationContract.State.Loading,
            SberPayConfirmationContract.Action.LoadSberPayInfoSuccess(
                "sberPayApiKey",
                "merchantLogin",
                "orderId",
                "orderNumber"
            )
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expectedEffect)
    }

    @Test
    fun `Should show LoadSberPayInfoFailed state with Loading state and LoadSberPayInfoFailed action`() {
        val expectedState = SberPayConfirmationContract.State.LoadSberPayInfoFailed(error)
        val out = logic(
            SberPayConfirmationContract.State.Loading,
            SberPayConfirmationContract.Action.LoadSberPayInfoFailed(error)
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showState).invoke(expectedState)
    }

    @Test
    fun `Should show Loading state with LoadSberPayInfoFailed state and LoadSberPayInfo action`() {
        val expectedState = SberPayConfirmationContract.State.Loading
        val out = logic(
            SberPayConfirmationContract.State.LoadSberPayInfoFailed(error),
            SberPayConfirmationContract.Action.LoadSberPayInfo
        )

        runBlocking {
            // when
            out.sources.func()

            // then
            verify(showState).invoke(expectedState)
            verify(sberPayConfirmationInteractor).loadSberPayInfo(confirmationData, shopId)
        }
    }
}