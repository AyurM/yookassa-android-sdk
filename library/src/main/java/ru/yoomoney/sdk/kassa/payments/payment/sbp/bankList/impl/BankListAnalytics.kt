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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl

import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action.PaymentProcessFinished
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action.SelectBank
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action.LoadBankListSuccess
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.State
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out

internal const val SBP_CONFIRMATION_ACTION = "actionSBPConfirmation"
private const val ACTION_SELECT_ORDINARY_BANK = "actionSelectOrdinaryBank"
private const val SHOW_BANK_FULL_LIST = "showBankFullList"

internal class BankListAnalytics(
    private val reporter: Reporter,
    private val businessLogic: Logic<State, BankList.Action>,
) : Logic<State, BankList.Action> {

    override fun invoke(
        state: State,
        action: BankList.Action,
    ): Out<State, BankList.Action> {
        val nameArgsPairs = when {

            action is LoadBankListSuccess && state is State.Progress -> {
                listOf(SHOW_BANK_FULL_LIST to null)
            }

            action is SelectBank && state is State.BankListContent -> {
                val isUniversalLink = action.url.startsWith("http")
                reporter.report(ACTION_SELECT_ORDINARY_BANK, !isUniversalLink)
                listOf(null to null)
            }

            action is PaymentProcessFinished && state is State.BankListStatusProgress -> {
                listOf(SBP_CONFIRMATION_ACTION to null)
            }

            else -> listOf(null to null)
        }

        nameArgsPairs.forEach { pair ->
            pair.first?.let {
                reporter.report(it, pair.second)
            }
        }

        return businessLogic(state, action)
    }

}