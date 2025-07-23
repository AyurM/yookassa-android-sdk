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

package ru.yoomoney.sdk.kassa.payments.paymentAuth

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.databinding.YmFragmentPaymentAuthBinding
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Action
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.Effect
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuth.State
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.march.RuntimeViewModel
import javax.inject.Inject

internal typealias PaymentAuthViewModel = RuntimeViewModel<State, Action, Effect>

internal class PaymentAuthFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    private val amount: Amount by lazy {
        arguments?.getParcelable(AMOUNT_KEY) as? Amount ?: throw IllegalStateException("AMOUNT_KEY should be passed")
    }

    private val linkWalletToAp: Boolean by lazy {
        arguments?.getBoolean(LINK_WALLET_KEY) ?: throw IllegalStateException("LINK_WALLET_KEY should be passed")
    }

    private var _binding: YmFragmentPaymentAuthBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onAttach(context: Context) {
        CheckoutInjector.injectPaymentAuthFragment(this)
        super.onAttach(context)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = YmFragmentPaymentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            // can't restore ui state in right way, dismiss and send cancel result
            requireActivity().setResult(Activity.RESULT_CANCELED)
            (parentFragment as? MainDialogFragment)?.dismiss()
        }
        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MoneyPaymentComposeContent {
                    PaymentAuthScreen(
                        errorFormatter,
                        linkWalletToAp,
                        amount,
                        viewModelFactory,
                        onSuccess = { finishWithResult(Screen.PaymentAuth.PaymentAuthResult.SUCCESS) },
                        onCancel = { finishWithResult(Screen.PaymentAuth.PaymentAuthResult.CANCEL) }
                    )
                }
            }
        }
    }

    private fun finishWithResult(paymentAuthResult: Screen.PaymentAuth.PaymentAuthResult) {
        setFragmentResult(PAYMENT_AUTH_RESULT_KEY, bundleOf(PAYMENT_AUTH_RESULT_EXTRA to paymentAuthResult))
        parentFragmentManager.popBackStack()
    }

    companion object {

        const val PAYMENT_AUTH_RESULT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.PAYMENT_AUTH_RESULT_KEY"
        const val PAYMENT_AUTH_RESULT_EXTRA =
            "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.PAYMENT_AUTH_RESULT_EXTRA"

        private const val AMOUNT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.AMOUNT_KEY"
        private const val LINK_WALLET_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.LINK_WALLET_KEY"

        fun createFragment(amount: Amount, linkWalletToApp: Boolean): PaymentAuthFragment {
            return PaymentAuthFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(AMOUNT_KEY, amount)
                    putBoolean(LINK_WALLET_KEY, linkWalletToApp)
                }
            }
        }
    }
}

internal fun Long.toTimeString(): String {
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}