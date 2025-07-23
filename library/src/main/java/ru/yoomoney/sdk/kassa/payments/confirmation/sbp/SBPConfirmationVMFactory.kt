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

package ru.yoomoney.sdk.kassa.payments.confirmation.sbp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl.SBPConfirmationBusinessLogic
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.impl.SBPConfirmationUseCase
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentDetailsModule
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

internal class SBPConfirmationVMFactory @AssistedInject constructor(
    private val sbpConfirmationUseCase: SBPConfirmationUseCase,
    @Assisted
    private val confirmationData: String,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RuntimeViewModel<SBPConfirmationContract.State, SBPConfirmationContract.Action, SBPConfirmationContract.Effect>(
            featureName = PaymentDetailsModule.PAYMENT_DETAILS,
            initial = {
                Out(SBPConfirmationContract.State.Loading) {
                    input { showState(state) }
                    input { sbpConfirmationUseCase.getConfirmationDetails(confirmationData) }
                }
            },
            logic = {
                SBPConfirmationBusinessLogic(
                    showState = showState,
                    showEffect = showEffect,
                    source = source,
                    paymentDetailsUseCase = sbpConfirmationUseCase
                )
            }
        ) as T
    }

    @AssistedFactory
    internal interface AssistedSBPConfirmationVMFactory {
        fun create(confirmationData: String): SBPConfirmationVMFactory
    }
}