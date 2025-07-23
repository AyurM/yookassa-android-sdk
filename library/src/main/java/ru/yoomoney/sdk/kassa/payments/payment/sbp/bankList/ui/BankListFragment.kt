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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ru.yoomoney.sdk.kassa.payments.databinding.YmFragmentSbpBankListBinding
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListInteractor
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModelFactory
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import javax.inject.Inject

private const val CONFIRMATION_URL_KEY = "CONFIRMATION_URL_KEY"
private const val PAYMENT_ID_KEY = "PAYMENT_ID_KEY"

internal class BankListFragment : Fragment() {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: BankListViewModelFactory.AssistedBankListVmFactory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var bankListInteractor: BankListInteractor

    private var _binding: YmFragmentSbpBankListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val confirmationUrl: String by lazy { arguments?.getString(CONFIRMATION_URL_KEY).orEmpty() }
    private val paymentId: String by lazy { arguments?.getString(PAYMENT_ID_KEY).orEmpty() }

    private val bankListParams: BankListViewModelFactory.BankListAssistedParams by lazy {
        BankListViewModelFactory.BankListAssistedParams(
            confirmationUrl,
            paymentId
        )
    }

    override fun onAttach(context: Context) {
        CheckoutInjector.injectBankListFragment(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = YmFragmentSbpBankListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MoneyPaymentComposeContent {
                    BankListController(
                        bankListParams,
                        errorFormatter,
                        viewModelFactory,
                        bankListInteractor,
                        closeBankList = { (parentFragment as? MainDialogFragment)?.dismiss() },
                        closeBankListWithFinish = { router.navigateTo(Screen.SBPConfirmationSuccessful) },
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(confirmationUrl: String, paymentId: String): BankListFragment {
            return BankListFragment().apply {
                arguments = bundleOf(
                    CONFIRMATION_URL_KEY to confirmationUrl,
                    PAYMENT_ID_KEY to paymentId
                )
            }
        }
    }
}