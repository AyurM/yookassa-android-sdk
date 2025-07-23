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

package ru.yoomoney.sdk.kassa.payments.confirmation.sbp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.SBPConfirmationVMFactory
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.compose.fragmentComposeView
import javax.inject.Inject

internal class SBPConfirmationFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: SBPConfirmationVMFactory.AssistedSBPConfirmationVMFactory

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var router: Router

    private val confirmationData: String by lazy {
        requireNotNull(
            arguments?.getString(
                CONFIRMATION_DATA
            )
        )
    }

    override fun onAttach(context: Context) {
        CheckoutInjector.injectConfirmationFragment(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        fragmentComposeView(
            context = requireContext(),
            content = {
                SBPConfirmationController(
                    navigateToBankList = ::navigateToBankList,
                    confirmationData = confirmationData,
                    navigateToSuccessful = { router.navigateTo(Screen.SBPConfirmationSuccessful) },
                    viewModelFactory = viewModelFactory,
                    errorFormatter = errorFormatter
                )
            }
        )

    private fun navigateToBankList(confirmationUrl: String, paymentId: String) {
        router.navigateTo(Screen.BankList(confirmationUrl, paymentId))
    }

    companion object {
        private const val CONFIRMATION_DATA = "ru.yoomoney.sdk.kassa.payments.confirmation.CONFIRMATION_DATA"

        fun createFragment(confirmationData: String): SBPConfirmationFragment {
            return SBPConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString(CONFIRMATION_DATA, confirmationData)
                }
            }
        }
    }
}