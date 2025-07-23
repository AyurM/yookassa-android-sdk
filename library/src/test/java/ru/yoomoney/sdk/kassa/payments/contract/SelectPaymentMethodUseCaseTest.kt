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

package ru.yoomoney.sdk.kassa.payments.contract

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import ru.yoomoney.sdk.auth.api.account.AccountRepository
import ru.yoomoney.sdk.kassa.payments.model.SelectedOptionNotFoundException
import ru.yoomoney.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.userAuth.UserAuthInfoRepository

class SelectPaymentMethodUseCaseTest {

    private val getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository = mock()
    private val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway = mock()
    private val accountRepository: AccountRepository = mock()
    private val userAuthInfoRepository: UserAuthInfoRepository = mock()

    private val interactor = SelectPaymentMethodUseCaseImpl(
        getLoadedPaymentOptionListRepository,
        checkPaymentAuthRequiredGateway,
        accountRepository,
        userAuthInfoRepository,
    )
    private val paymentOptionId = 123
    private val instrumentId: String? = null

    @Test
    fun `should return LoadContractFailed action for when error load paymentOptions`() {
        // given
        whenever(getLoadedPaymentOptionListRepository.getLoadedPaymentOptions()).thenReturn(emptyList())
        val expected = Contract.Action.LoadContractFailed(SelectedOptionNotFoundException(paymentOptionId))

        // when
        val actual = runBlocking { interactor.select(paymentOptionId, instrumentId) }

        // then
        Assertions.assertThat(actual).isEqualTo(expected)
    }
}