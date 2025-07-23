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

package ru.yoomoney.sdk.kassa.payments.unbind.ui

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
import ru.yoomoney.sdk.kassa.payments.databinding.YmFragmentUnbindCardBinding
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardContract
import ru.yoomoney.sdk.kassa.payments.unbind.impl.UnbindCardScreenController
import ru.yoomoney.sdk.march.RuntimeViewModel
import javax.inject.Inject

internal typealias UnbindCardViewModel = RuntimeViewModel<UnbindCardContract.State, UnbindCardContract.Action, UnbindCardContract.Effect>

internal class UnbindCardFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    private val linkedCard by lazy { arguments?.getParcelable<LinkedCard>(PAYMENT_OPTION_LINKED_CARD) }
    private val instrumental by lazy { arguments?.getParcelable<PaymentInstrumentBankCard>(PAYMENT_OPTION_INSTRUMENT) }

    private var _binding: YmFragmentUnbindCardBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onAttach(context: Context) {
        CheckoutInjector.injectUntieCardFragment(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = YmFragmentUnbindCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MoneyPaymentComposeContent {
                    UnbindCardScreenController(
                        linkedCard,
                        instrumental,
                        viewModelFactory,
                        onUnbindSuccess = { last4 ->
                            setResult(last4)
                            closeScreen()
                        },
                        onCloseScreen = {
                            closeScreen()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setResult(last4: String) {
        setFragmentResult(
            UNBIND_CARD_RESULT_KEY,
            bundleOf(
                UNBIND_CARD_RESULT_EXTRA to Screen.UnbindInstrument.Success(
                    last4,
                ),
            ),
        )
    }

    private fun closeScreen() {
        parentFragmentManager.popBackStack()
        router.navigateTo(Screen.PaymentOptions())
    }

    companion object {
        const val UNBIND_CARD_RESULT_KEY = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.UNBIND_CARD_RESULT_KEY"
        const val UNBIND_CARD_RESULT_EXTRA = "ru.yoomoney.sdk.kassa.payments.impl.paymentAuth.UNBIND_CARD_RESULT_EXTRA"

        private const val PAYMENT_OPTION_LINKED_CARD =
            "ru.yoomoney.sdk.kassa.payments.unbind.PAYMENT_OPTION_LINKED_CARD"
        private const val PAYMENT_OPTION_INSTRUMENT = "ru.yoomoney.sdk.kassa.payments.unbind.PAYMENT_OPTION_INSTRUMENT"

        fun createFragment(linkedCard: LinkedCard): UnbindCardFragment {
            return UnbindCardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PAYMENT_OPTION_LINKED_CARD, linkedCard)
                }
            }
        }

        fun createFragment(instrumentBankCard: PaymentInstrumentBankCard): UnbindCardFragment {
            return UnbindCardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PAYMENT_OPTION_INSTRUMENT, instrumentBankCard)
                }
            }
        }
    }
}
