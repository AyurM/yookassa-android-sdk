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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Effect
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.State
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain
import ru.yoomoney.sdk.kassa.payments.utils.func


internal class BankListBusinessLogicInvokeTest {
    private val showState: (State) -> Action = mock()
    private val showEffect: (Effect) -> Unit = mock()
    private val source: () -> Action = mock()
    private val confirmationUrl = "www.yoomoney.ru/confirm"
    private val paymentId = "12345"
    private val bankListInteractor: BankListInteractor = mock()
    private val throwable = Throwable()
    private val bankUrl: String = "yoomoney.ru/pay"
    private val bankList = listOf(
        SbpWidgetBankDomain("name1", "logo", "url"),
        SbpWidgetBankDomain("name2", "logo", "url"),
        SbpWidgetBankDomain("name3", "logo", "url"),
    )

    private val logic =
        BankListBusinessLogic(showState, showEffect, source, bankListInteractor, confirmationUrl, paymentId)

    @Test
    fun `should show bankListContent state with loading and LoadBankListSuccess action`() {
        runBlocking {
            val expectedState = State.BankListContent(bankList)
            val out = logic(State.Progress, Action.LoadBankListSuccess(bankList))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show Error state with loading and getSbpBanks`() {
        runBlocking {

            val expectedState = State.Error(throwable)
            whenever(bankListInteractor.getSbpWidgetBanks(paymentId)).doReturn(Action.LoadBankListFailed(throwable))
            val out = logic(State.Progress, bankListInteractor.getSbpWidgetBanks(paymentId))

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getSbpWidgetBanks(paymentId)
        }
    }

    @Test
    fun `should show Progress state with Error state and LoadBankList action`() {
        runBlocking {
            val expectedState = State.Progress
            whenever(bankListInteractor.getSbpWidgetBanks(paymentId)).doReturn(Action.LoadBankListSuccess(bankList))
            val out = logic(State.Error(throwable), Action.LoadBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getSbpWidgetBanks(paymentId)
        }
    }

    @Test
    fun `should show CloseBankList effect and BackToBankList action`() {
        runBlocking {
            val out = logic(State.BankListContent(bankList), Action.BackToBankList)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankList)
            verify(source).invoke()
        }
    }

    @Test
    fun `should show OpenBank effect and SelectBank action`() {
        runBlocking {
            val out = logic(State.BankListContent(bankList), Action.SelectBank(bankUrl))

            out.sources.func()

            verify(showEffect).invoke(Effect.OpenBank(bankUrl))
            verify(source).invoke()
        }
    }

    @Test
    fun `should show BankListStatusProgress state with BankListContent state and BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.BankListStatusProgress(bankList)
            val out = logic(State.BankListContent(bankList), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getPaymentStatus(paymentId)
        }
    }

    @Test
    fun `should show BankListContent state with BankListStatusProgress state and  PaymentProcessInProgress action`() {
        runBlocking {
            val expectedState = State.BankListContent(bankList)
            val out = logic(State.BankListStatusProgress(bankList), Action.PaymentProcessInProgress)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show CloseBankListWithFinish effect with BankListStatusProgress state and PaymentProcessFinished action`() {
        runBlocking {
            val out = logic(State.BankListStatusProgress(bankList), Action.PaymentProcessFinished)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankListWithFinish)
            verify(source).invoke()
        }
    }

    @Test
    fun `should show Progress state with Error state and  LoadBankList action`() {
        runBlocking {
            val expectedState = State.Progress
            val out = logic(State.Error(throwable), Action.LoadBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show BankListStatusProgress state with PaymentBankListStatusError state and LoadPaymentStatus action`() {
        runBlocking {
            val expectedState = State.BankListStatusProgress(bankList)
            val out = logic(State.PaymentBankListStatusError(throwable, bankList), Action.LoadPaymentStatus)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show BankListStatusProgress state with PaymentBankListStatusError state and BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.BankListStatusProgress(bankList)
            val out = logic(State.PaymentBankListStatusError(throwable, bankList), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ActivityNotFoundError state with PaymentBankList state and ActivityNotFound action`() {
        runBlocking {
            val bankListContent = State.BankListContent(bankList)
            val expectedState = State.ActivityNotFoundError(throwable, bankListContent)
            val out = logic(bankListContent, Action.ActivityNotFound(throwable))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ShortBankListState state with ActivityNotFoundState state and BackToBankList action`() {
        runBlocking {
            val expectedState = State.BankListContent(bankList)
            val activityNotFoundError = State.ActivityNotFoundError(throwable, expectedState)
            val out = logic(activityNotFoundError, Action.BackToBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }
}