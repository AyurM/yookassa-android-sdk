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
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl.SBPConfirmationBusinessLogic
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationContract
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl.SBPConfirmationUseCase
import ru.yoomoney.sdk.kassa.payments.model.PaymentStatus
import ru.yoomoney.sdk.kassa.payments.model.UserPaymentProcess
import ru.yoomoney.sdk.march.Effect

internal class ConfirmationDetailsBusinessLogicEffectTest {

    private val showState: (SBPConfirmationContract.State) -> SBPConfirmationContract.Action = mock()
    private val showEffect: (SBPConfirmationContract.Effect) -> Unit = mock()
    private val source: () -> SBPConfirmationContract.Action = mock()
    private val useCase: SBPConfirmationUseCase = mock()

    private val logic =
        SBPConfirmationBusinessLogic(
            showState = { showState(it) },
            showEffect = { showEffect(it) },
            source = { source() },
            paymentDetailsUseCase = useCase
        )

    @Test
    fun `Should send SendFinishState effect with Loading state and GetPaymentDetailsSuccess action`() {
        // given
        val expected = SBPConfirmationContract.Effect.SendFinishState
        val out = logic(
            SBPConfirmationContract.State.Loading,
            SBPConfirmationContract.Action.GetPaymentDetailsSuccess(
                "url",
                "paymentId",
                PaymentStatus.SUCCEEDED,
                UserPaymentProcess.FINISHED
            )
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
    }

    @Test
    fun `Should send ContinueSBPConfirmation effect with Loading state and GetPaymentDetailsSuccess action`() {
        // given
        val url = "url"
        val expected = SBPConfirmationContract.Effect.ContinueSBPConfirmation(url, "paymentId")
        val out = logic(
            SBPConfirmationContract.State.Loading,
            SBPConfirmationContract.Action.GetPaymentDetailsSuccess(
                url,
                "paymentId",
                PaymentStatus.SUCCEEDED,
                UserPaymentProcess.IN_PROGRESS
            )
        )

        // when
        runBlocking { out.sources.func() }

        // then
        verify(showEffect).invoke(expected)
    }


    private suspend fun <E> List<Effect<E>>.func() {
        forEach {
            when (it) {
                is Effect.Input.Fun -> it.func()
                is Effect.Output -> it.func()
                else -> error("unexpected")
            }
        }
    }
}