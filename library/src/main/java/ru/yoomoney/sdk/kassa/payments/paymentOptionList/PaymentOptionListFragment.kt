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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.databinding.YmFragmentPaymentOptionsBinding
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsListFormatter
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsViewModel
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsViewModelFactory
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.getAdditionalInfo
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderIcon
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderTitle
import ru.yoomoney.sdk.kassa.payments.extensions.showSnackbar
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutAlertDialog
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeConfig
import ru.yoomoney.sdk.kassa.payments.ui.swipe.SwipeItemHelper
import ru.yoomoney.sdk.kassa.payments.ui.view.LoadingView
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.utils.getBankOrBrandLogo
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject
import ru.yoomoney.sdk.gui.gui.R as GuiResources

private const val PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID"
internal class PaymentOptionListFragment :
    Fragment(),
    PaymentOptionListRecyclerViewAdapter.PaymentOptionClickListener {

    @Inject
    lateinit var uiParameters: UiParameters

    @Inject
    @PaymentOptionsListFormatter
    lateinit var errorFormatter: ErrorFormatter

    private val paymentMethodId: String? by lazy {
        arguments?.getString(PAYMENT_METHOD_ID)
    }

    @Inject
    lateinit var viewModelFactory: PaymentOptionsViewModelFactory.AssistedPaymentOptionVmFactory

    private val viewModel: PaymentOptionsViewModel by viewModel(PaymentOptionsViewModelFactory.PAYMENT_OPTIONS_LIST) {
        viewModelFactory.create(
            PaymentOptionsViewModelFactory.PaymentOptionsAssisted(
                paymentMethodId
            )
        )
    }

    @Inject
    lateinit var router: Router

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: LoadingView
    private lateinit var errorComposeView: ComposeView

    private val swipeItemHelper: SwipeItemHelper by lazy {
        val resources = requireContext().resources
        val swipeConfig = SwipeConfig.get(
            resources.getInteger(android.R.integer.config_shortAnimTime),
            resources.getDimensionPixelSize(GuiResources.dimen.ym_space5XL),
            MENU_ITEM_COUNT
        )
        SwipeItemHelper(requireContext(), swipeConfig)
    }

    private var _binding: YmFragmentPaymentOptionsBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onAttach(context: Context) {
        CheckoutInjector.injectPaymentOptionListFragment(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = YmFragmentPaymentOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resources = view.resources
        val isTablet = resources.getBoolean(R.bool.ym_isTablet)
        val minHeight = resources.getDimensionPixelSize(R.dimen.ym_viewAnimator_maxHeight).takeIf { !isTablet }
        val minLoadingHeight =
            resources.getDimensionPixelSize(R.dimen.ym_payment_options_loading_min_height).takeIf { !isTablet }

        setFragmentResultListener(MoneyAuthFragment.MONEY_AUTH_RESULT_KEY) { _, bundle ->
            val result = bundle.getSerializable(MoneyAuthFragment.MONEY_AUTH_RESULT_EXTRA) as Screen.MoneyAuth.Result
            onAuthResult(result)
        }

        setFragmentResultListener(UnbindCardFragment.UNBIND_CARD_RESULT_KEY) { _, bundle ->
            val result =
                requireNotNull(bundle.getParcelable<Screen.UnbindInstrument.Success>(UnbindCardFragment.UNBIND_CARD_RESULT_EXTRA))
            onUnbindingCardResult(result.panUnbindingCard)
        }

        recyclerView = RecyclerView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        loadingView = LoadingView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minLoadingHeight ?: MATCH_PARENT, Gravity.CENTER)
        }

        errorComposeView = ComposeView(view.context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minHeight ?: MATCH_PARENT, Gravity.CENTER)
        }

        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = ::showError
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.contentContainer.removeAllViews()
        swipeItemHelper.detachFromRecyclerView()
        _binding = null
    }

    override fun onPaymentOptionClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.ProceedWithPaymentMethod(optionId, instrumentId))
    }

    override fun onOptionsMenuClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.OpenUnbindScreen(optionId, instrumentId))
    }

    override fun onDeleteClick(optionId: Int, instrumentId: String?) {
        viewModel.handleAction(PaymentOptionList.Action.OpenUnbindingAlert(optionId, instrumentId))
    }

    fun onAppear() {
        viewModel.handleAction(
            PaymentOptionList.Action.Load
        )
    }

    private fun onAuthResult(paymentAuthResult: Screen.MoneyAuth.Result) {
        viewModel.handleAction(
            when (paymentAuthResult) {
                Screen.MoneyAuth.Result.SUCCESS -> PaymentOptionList.Action.PaymentAuthSuccess
                Screen.MoneyAuth.Result.CANCEL -> PaymentOptionList.Action.PaymentAuthCancel
            }
        )
    }

    private fun onUnbindingCardResult(panUnbindingCard: String) {
        view?.showSnackbar(
            message = getString(
                R.string.ym_unbinding_card_success,
                panUnbindingCard.takeLast(4)
            ),
            textColorResId = GuiResources.color.color_type_inverse,
            backgroundColorResId = GuiResources.color.color_type_success
        )
    }

    private fun showState(state: PaymentOptionList.State) {
        Picasso.get().load(Uri.parse(state.yooMoneyLogoUrl))
            .placeholder(binding.topBar.logo.drawable)
            .into(binding.topBar.logo)
        showState(!isTablet) {
            when (state) {
                is PaymentOptionList.State.Loading -> showLoadingState()
                is PaymentOptionList.State.Content -> showContentState(state.content)
                is PaymentOptionList.State.Error -> showErrorState(state)
                is PaymentOptionList.State.ContentWithUnbindingAlert -> showContentWithUnbindingAlert(state)
                else -> Unit
            }
        }
    }

    private fun showContentWithUnbindingAlert(state: PaymentOptionList.State.ContentWithUnbindingAlert) {
        showContentState(state.content)
        showAlert(state)
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(binding.contentContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showAlert(state: PaymentOptionList.State.ContentWithUnbindingAlert) {
        val context = requireContext()
        AlertDialog.Builder(context, R.style.ym_DialogStyleColored)
            .setMessage(context.getString(R.string.ym_unbinding_alert_message))
            .setPositiveButton(R.string.ym_unbind_card_action) { dialog, _ ->
                dialog.dismiss()
                actionOnDialog(
                    PaymentOptionList.Action.ClickOnUnbind(state.optionId, state.instrumentId)
                )
            }
            .setNegativeButton(R.string.ym_logout_dialog_button_negative) { dialog, _ ->
                dialog.dismiss()
                actionOnDialog(PaymentOptionList.Action.ClickOnCancel)
            }
            .setOnCancelListener {
                actionOnDialog(PaymentOptionList.Action.ClickOnCancel)
            }
            .show()
    }

    private fun actionOnDialog(action: PaymentOptionList.Action) {
        swipeItemHelper.forceCancel()
        viewModel.handleAction(action)
    }

    private fun showLoadingState() {
        binding.topBar.isLogoVisible = uiParameters.showLogo
        replaceDynamicView(loadingView)
    }

    private fun showContentState(content: PaymentOptionListOutputModel) {
        when (content) {
            is PaymentOptionListSuccessOutputModel -> showPaymentOptions(content)
            is PaymentOptionListNoWalletOutputModel -> showAuthNoWalletViewModel()
        }
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = binding.contentContainer.getViewHeight() }
    }

    private fun showPaymentOptions(content: PaymentOptionListSuccessOutputModel) {
        val listItems: List<PaymentOptionListItem> = content.options.map {
            it.getPaymentOptionListItems(requireContext())
        }.flatten()

        binding.topBar.isLogoVisible = uiParameters.showLogo
        replaceDynamicView(recyclerView)
        recyclerView.adapter = PaymentOptionListRecyclerViewAdapter(this, listItems)
        swipeItemHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAuthNoWalletViewModel() {
        if (!isStateSaved) {
            val content = YmAlertDialog.DialogContent(
                content = getString(
                    R.string.ym_no_wallet_dialog_message
                ),
                actionPositiveText = getString(R.string.ym_no_wallet_dialog_shoose_payment_option)
            )

            CheckoutAlertDialog.create(
                manager = childFragmentManager,
                content = content,
                shouldColorPositiveColor = true,
                dimAmount = 0.6f
            ).apply {
                attachListener(object : YmAlertDialog.DialogListener {
                    override fun onPositiveClick() {
                        viewModel.handleAction(PaymentOptionList.Action.Logout)
                    }
                })
            }.show(childFragmentManager)
        }
    }

    private fun showErrorState(state: PaymentOptionList.State.Error) {
        binding.topBar.isLogoVisible = uiParameters.showLogo
        showError(state.error)
        loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = binding.contentContainer.getViewHeight() }
    }

    private fun showError(throwable: Throwable) {
        replaceDynamicView(errorComposeView)
        errorComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MoneyPaymentComposeContent {
                    ErrorStateScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        action = stringResource(id = R.string.ym_retry),
                        subtitle = errorFormatter.format(throwable).toString()
                    ) {
                        viewModel.handleAction(
                            PaymentOptionList.Action.Load
                        )
                    }
                }
            }
        }
    }

    private fun showEffect(effect: PaymentOptionList.Effect) {
        when (effect) {
            is PaymentOptionList.Effect.ShowContract -> router.navigateTo(Screen.Contract(effect.paymentOptionId, effect.instrumentId))
            is PaymentOptionList.Effect.StartTokenization -> router.navigateTo(Screen.Tokenize(effect.tokenizeInputModel))
            PaymentOptionList.Effect.RequireAuth -> {
                showLoadingState()
                router.navigateTo(Screen.MoneyAuth)
            }

            is PaymentOptionList.Effect.Cancel -> router.navigateTo(Screen.TokenizeCancelled)
            is PaymentOptionList.Effect.UnbindLinkedCard -> router.navigateTo(Screen.UnbindLinkedCard(effect.paymentOption))
            is PaymentOptionList.Effect.UnbindInstrument -> router.navigateTo(Screen.UnbindInstrument(effect.instrumentBankCard))
            is PaymentOptionList.Effect.UnbindFailed -> showSnackBar(effect.instrumentBankCard, false)
            is PaymentOptionList.Effect.UnbindSuccess -> showSnackBar(effect.instrumentBankCard, true)
        }
    }

    private fun showSnackBar(instrumentBankCard: PaymentInstrumentBankCard, isUnbindSuccess: Boolean) {
        if (isUnbindSuccess) {
            view?.showSnackbar(
                message = getString(
                    R.string.ym_unbinding_card_success,
                    instrumentBankCard.last4
                ),
                textColorResId = GuiResources.color.color_type_inverse,
                backgroundColorResId = GuiResources.color.color_type_success
            )
        } else {
            view?.showSnackbar(
                message = getString(R.string.ym_unbinding_card_failed, instrumentBankCard.last4),
                textColorResId = GuiResources.color.color_type_inverse,
                backgroundColorResId = GuiResources.color.color_type_alert
            )
        }
    }

    private fun replaceDynamicView(view: View) {
        binding.contentContainer.getChildAt(0)?.also {
            if (it === view) {
                return
            }
            binding.contentContainer.removeView(it)
        }
        binding.contentContainer.addView(view)
    }

    companion object {
        private const val MENU_ITEM_COUNT = 1

        fun create(paymentMethodId: String?): Fragment = PaymentOptionListFragment().apply {
            arguments = bundleOf(PAYMENT_METHOD_ID to paymentMethodId)
        }
    }
}

private fun PaymentOption.getPaymentOptionListItems(context: Context): List<PaymentOptionListItem> {
    val instruments = (this as? BankCardPaymentOption)?.getInstrumentListItems(context) ?: emptyList()
    return instruments + PaymentOptionListItem(
        optionId = id,
        additionalInfo = getAdditionalInfo(context).let { info ->
            info?.takeIf { _ -> this is Wallet }
                ?.makeStartMedium(context) ?: info
        },
        canLogout = this is Wallet,
        hasOptions = this is LinkedCard,
        isWalletLinked = this is LinkedCard && this.isLinkedToWallet,
        title = this.title ?: getPlaceholderTitle(context),
        urlLogo = this.icon,
        logo = getPlaceholderIcon(context)
    )
}

private fun BankCardPaymentOption.getInstrumentListItems(context: Context): List<PaymentOptionListItem> {
    return paymentInstruments.map { paymentInstrument ->
        PaymentOptionListItem(
            optionId = id,
            instrumentId = paymentInstrument.paymentInstrumentId,
            additionalInfo = context.resources.getString(R.string.ym_linked_not_wallet_card),
            canLogout = false,
            hasOptions = true,
            isWalletLinked = false,
            logo = requireNotNull(
                ContextCompat.getDrawable(
                    context,
                    getBankOrBrandLogo(paymentInstrument.cardNumber, paymentInstrument.cardType)
                )
            ),
            title = "•••• " + paymentInstrument.last4,
            urlLogo = null
        )
    }
}

private fun CharSequence.makeStartMedium(context: Context) =
    (this as? Spannable ?: SpannableStringBuilder(this)).apply {
        setSpan(
            TextAppearanceSpan(context, GuiResources.style.Text_Caption1_Medium),
            0,
            length - 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
