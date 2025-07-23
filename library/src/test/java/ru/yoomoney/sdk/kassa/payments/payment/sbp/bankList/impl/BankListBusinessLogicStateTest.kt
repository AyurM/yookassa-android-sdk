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

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.State
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain
import ru.yoomoney.sdk.march.generateBusinessLogicTests

@RunWith(Parameterized::class)
internal class BankListBusinessLogicStateTest(
    @Suppress("unused")
    val testName: String,
    val state: State,
    val action: Action,
    val expected: State,
) {

    companion object {
        private val confirmationUrl = "www.yoomoney.ru/confirm"
        private val paymentId = "12345"
        private val throwable = Throwable()
        private val bankDeeplink: String = "yoomoney.ru/pay"
        private val bankList = listOf(
            SbpWidgetBankDomain("name1", "logo", "url"),
            SbpWidgetBankDomain("name2", "logo", "url"),
            SbpWidgetBankDomain("name3", "logo", "url"),
        )

        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val errorState = State.Error(throwable)
            val bankListContentState = State.BankListContent(bankList)
            val progressState = State.Progress
            val bankListStatusProgress = State.BankListStatusProgress(bankList)
            val paymentBankListStatusError = State.PaymentBankListStatusError(throwable, bankList)
            val loadBankListAction = Action.LoadBankList
            val loadBankListFailedAction = Action.LoadBankListFailed(throwable)
            val loadBankListSuccessAction = Action.LoadBankListSuccess(bankList)
            val backToBankListAction = Action.BackToBankList
            val loadPaymentStatusAction = Action.LoadPaymentStatus
            val paymentStatusErrorAction = Action.PaymentStatusError(throwable)
            val bankInteractionFinished = Action.BankInteractionFinished
            val paymentProcessFinished = Action.PaymentProcessFinished
            val selectBankAction = Action.SelectBank(bankDeeplink)
            val paymentProcessInProgress = Action.PaymentProcessInProgress
            val activityNotFoundState = State.ActivityNotFoundError(throwable, bankListContentState)
            val activityNotFoundAction = Action.ActivityNotFound(throwable)
            val searchAction = Action.Search("sber")
            val cancelSearchAction = Action.CancelSearch
            return generateBusinessLogicTests<State, Action>(
                generateState = { kClassState ->
                    when (kClassState) {
                        State.Error::class -> errorState
                        State.BankListContent::class -> bankListContentState
                        State.Progress::class -> progressState
                        State.BankListStatusProgress::class -> bankListStatusProgress
                        State.PaymentBankListStatusError::class -> paymentBankListStatusError
                        State.ActivityNotFoundError::class -> activityNotFoundState
                        else -> kClassState.objectInstance ?: error(kClassState)
                    }
                },
                generateAction = { kClassAction ->
                    when (kClassAction) {
                        Action.LoadBankList::class -> loadBankListAction
                        Action.LoadBankListFailed::class -> loadBankListFailedAction
                        Action.LoadBankListSuccess::class -> loadBankListSuccessAction
                        Action.BackToBankList::class -> backToBankListAction
                        Action.LoadPaymentStatus::class -> loadPaymentStatusAction
                        Action.PaymentStatusError::class -> paymentStatusErrorAction
                        Action.BankInteractionFinished::class -> bankInteractionFinished
                        Action.PaymentProcessFinished::class -> paymentProcessFinished
                        Action.SelectBank::class -> selectBankAction
                        Action.PaymentProcessInProgress::class -> paymentProcessInProgress
                        Action.ActivityNotFound::class -> activityNotFoundAction
                        Action.Search::class -> searchAction
                        Action.CancelSearch::class -> cancelSearchAction
                        else -> kClassAction.objectInstance ?: error(kClassAction)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        progressState to loadBankListSuccessAction -> bankListContentState
                        progressState to loadBankListFailedAction -> errorState
                        bankListContentState to selectBankAction -> bankListContentState
                        bankListContentState to bankInteractionFinished -> bankListStatusProgress
                        bankListStatusProgress to paymentStatusErrorAction -> paymentBankListStatusError
                        paymentBankListStatusError to loadPaymentStatusAction -> bankListStatusProgress
                        bankListStatusProgress to paymentProcessInProgress -> bankListContentState
                        bankListStatusProgress to paymentProcessFinished -> bankListStatusProgress
                        bankListContentState to backToBankListAction -> bankListContentState
                        bankListContentState to loadBankListSuccessAction -> bankListContentState
                        errorState to loadBankListAction -> progressState
                        bankListContentState to activityNotFoundAction -> activityNotFoundState
                        activityNotFoundState to backToBankListAction -> bankListContentState
                        paymentBankListStatusError to bankInteractionFinished -> bankListStatusProgress
                        bankListContentState to searchAction -> bankListContentState.copy(searchText = searchAction.searchText)
                        bankListContentState to cancelSearchAction -> bankListContentState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = BankListBusinessLogic(
        showState = mock(),
        showEffect = mock(),
        source = mock(),
        interactor = mock(),
        confirmationUrl = confirmationUrl,
        paymentId = paymentId,
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertEquals(expected, actual.state)
    }
}