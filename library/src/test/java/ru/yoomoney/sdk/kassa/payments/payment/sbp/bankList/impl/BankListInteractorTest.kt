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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain

@RunWith(RobolectricTestRunner::class)
internal class BankListInteractorTest {

    private val bankList = listOf(
        SbpWidgetBankDomain("sber", "logo", "url"),
        SbpWidgetBankDomain("name2", "logo", "url"),
        SbpWidgetBankDomain("name3", "logo", "url"),
    )

    private val bankListInteractor = BankListInteractorImpl(BankListRepositoryImpl(mock(), mock()))

    @Test
    fun `should find only single bank result`() {
        runBlocking {
            val searchResult = bankListInteractor.searchBank("sber", bankList)
            assert(searchResult.size == 1)
        }
    }
}