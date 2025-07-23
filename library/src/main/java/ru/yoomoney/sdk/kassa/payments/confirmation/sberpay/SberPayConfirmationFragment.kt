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

package ru.yoomoney.sdk.kassa.payments.confirmation.sberpay

import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.utils.logSberPaySdk
import java.util.Locale
import javax.inject.Inject
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationVmFactory.Companion.SBER_PAY_CONFIRMATION_FEATURE
import ru.yoomoney.sdk.kassa.payments.databinding.YmSberpayConfirmationFragmentBinding
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.observe
import spay.sdk.SPaySdkApp
import spay.sdk.api.PaymentResult
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.updateLayoutParams
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.view.LoadingView
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners


internal class SberPayConfirmationFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: SberPayConfirmationVmFactory.AssistedSberPayVmFactory

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var testParameters: TestParameters

    private val viewModel: SberPayConfirmViewModel by viewModel(SBER_PAY_CONFIRMATION_FEATURE) {
        viewModelFactory.create(SberPayConfirmationVmFactory.SberPayAssistedParams(confirmationData, shopId))
    }

    private val confirmationData: String by lazy { requireNotNull(arguments?.getString(CONFIRMATION_DATA)) }

    private val shopId: String by lazy { requireNotNull(arguments?.getString(SHOP_ID)) }

    private var _binding: YmSberpayConfirmationFragmentBinding? = null
    private val binding get() = requireNotNull(_binding)

    private lateinit var errorComposeView: ComposeView
    private lateinit var loadingView: LoadingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SberPaySdkManager.init(requireContext().applicationContext as Application)
        CheckoutInjector.injectConfirmationFragment(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = YmSberpayConfirmationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resources = view.resources
        val isTablet = resources.getBoolean(R.bool.ym_isTablet)
        val minHeight = resources.getDimensionPixelSize(R.dimen.ym_viewAnimator_maxHeight).takeIf { !isTablet }

        loadingView = LoadingView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, minHeight ?: ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
        }

        errorComposeView = ComposeView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, minHeight ?: ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
        }

        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::handleState,
            onEffect = ::handleEffect,
            onFail = ::handleFail
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rootContainer.removeAllViews()
        _binding = null
    }

    private fun replaceDynamicView(view: View) {
        binding.rootContainer.getChildAt(0)?.also {
            if (it === view) {
                return
            }
            binding.rootContainer.removeView(it)
        }
        binding.rootContainer.addView(view)
    }

    private fun handleFail(throwable: Throwable) {
        showError(throwable)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = binding.rootContainer.getViewHeight() }
    }

    private fun showError(throwable: Throwable) {
        replaceDynamicView(errorComposeView)
        errorComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MoneyPaymentComposeContent {
                    ErrorStateScreen(
                        modifier = Modifier
                            .roundBottomSheetCorners()
                            .fillMaxSize(),
                        action = stringResource(id = R.string.ym_retry),
                        subtitle = errorFormatter.format(throwable).toString()
                    ) {
                        viewModel.handleAction(
                            SberPayConfirmationContract.Action.LoadSberPayInfo
                        )
                    }
                }
            }
        }
    }

    private fun handleEffect(effect: SberPayConfirmationContract.Effect) {
        when (effect) {
            is SberPayConfirmationContract.Effect.ShowSberPaySdk -> showSberPaySdk(
                effect.sberPayApikey,
                effect.merchantLogin,
                effect.orderId,
                effect.orderNumber
            )

            is SberPayConfirmationContract.Effect.CloseWithFinishState -> (parentFragment as? MainDialogFragment)?.dismiss()
        }
    }

    private fun handleState(state: SberPayConfirmationContract.State) {
        showState(!isTablet) {
            when (state) {
                is SberPayConfirmationContract.State.Loading -> replaceDynamicView(loadingView)
                is SberPayConfirmationContract.State.LoadSberPayInfoFailed -> handleFail(state.throwable)
            }
            loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = binding.rootContainer.getViewHeight() }
        }
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(binding.rootContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showSberPaySdk(
        sberPayApikey: String,
        merchantLogin: String,
        orderId: String,
        orderNumber: String
    ) {
        val isReadyForSPay = SberPaySdkManager.isReadyForSPay(requireContext().applicationContext)
        logSberPaySdk("is ready: $isReadyForSPay")
        if (isReadyForSPay) {
            SPaySdkApp.getInstance().payWithBankInvoiceId(
                context = requireContext(),
                apiKey = sberPayApikey,
                merchantLogin = merchantLogin,
                bankInvoiceId = orderId,
                orderNumber = orderNumber,
                appPackage = requireContext().packageName,
                language = Locale.getDefault().language,
                callback = { handleResult(it) }
            )
        } else {
            router.navigateTo(Screen.SberPaySdkConfirmationFailed(getString(R.string.ym_sber_sdk_not_ready)))
        }
    }

    private fun handleResult(paymentResult: PaymentResult) {
        logSberPaySdk(paymentResult.toString())
        when (paymentResult) {
            is PaymentResult.Success -> router.navigateTo(Screen.SberPaySdkConfirmationSuccessful)

            is PaymentResult.Error -> {
                val error = getString(R.string.ym_sber_sdk_error_prefix) + paymentResult.merchantError.toString()
                router.navigateTo(Screen.SberPaySdkConfirmationFailed(error))
            }

            is PaymentResult.Cancel -> viewModel.handleAction(SberPayConfirmationContract.Action.FinishedPaymentStatus)

            is PaymentResult.Processing -> {}
        }
    }

    companion object {
        private const val CONFIRMATION_DATA = "CONFIRMATION_DATA"
        private const val SHOP_ID = "SHOP_ID"

        fun createFragment(confirmationData: String, shopId: String): SberPayConfirmationFragment {
            return SberPayConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString(CONFIRMATION_DATA, confirmationData)
                    putString(SHOP_ID, shopId)
                }
            }
        }

    }
}