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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.squareup.picasso.Picasso
import ru.yoomoney.sdk.gui.dialog.YmAlertDialog
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Action
import ru.yoomoney.sdk.kassa.payments.contract.Contract.Effect
import ru.yoomoney.sdk.kassa.payments.contract.Contract.State
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule
import ru.yoomoney.sdk.kassa.payments.contract.savePayment.SavePaymentMethodInfoActivity
import ru.yoomoney.sdk.kassa.payments.databinding.YmFragmentContractBinding
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.configureForPhoneInput
import ru.yoomoney.sdk.kassa.payments.extensions.format
import ru.yoomoney.sdk.kassa.payments.extensions.getPlaceholderTitle
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.isPhoneNumber
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.extensions.visible
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardAnalyticsLogger
import ru.yoomoney.sdk.kassa.payments.metrics.bankCard.BankCardEvent
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.BankCardPaymentOption
import ru.yoomoney.sdk.kassa.payments.model.CardBrand
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode
import ru.yoomoney.sdk.kassa.payments.model.Fee
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.LinkedCardInfo
import ru.yoomoney.sdk.kassa.payments.model.MobileApplication
import ru.yoomoney.sdk.kassa.payments.model.PaymentIdCscConfirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.SBPInfo
import ru.yoomoney.sdk.kassa.payments.model.SberPay
import ru.yoomoney.sdk.kassa.payments.model.SbolSmsInvoicingInfo
import ru.yoomoney.sdk.kassa.payments.model.Wallet
import ru.yoomoney.sdk.kassa.payments.model.formatService
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.threeDS.WebViewActivity
import ru.yoomoney.sdk.kassa.payments.tokenize.ui.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutAlertDialog
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithAnimation
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.onCheckedChangedListener
import ru.yoomoney.sdk.kassa.payments.ui.resumePostponedTransition
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_CARD_NUMBER
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_MONTH
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_YEAR
import ru.yoomoney.sdk.kassa.payments.ui.view.cropToCircle
import ru.yoomoney.sdk.kassa.payments.utils.SimpleTextWatcher
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners
import ru.yoomoney.sdk.kassa.payments.utils.formatHtmlWithLinks
import ru.yoomoney.sdk.kassa.payments.utils.getBankOrBrandLogo
import ru.yoomoney.sdk.kassa.payments.utils.getMessageWithLink
import ru.yoomoney.sdk.kassa.payments.utils.show
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias ContractViewModel = RuntimeViewModel<State, Action, Effect>

private const val REQUEST_CODE_SCAN_BANK_CARD = 0x37BD
internal const val PAYMENT_OPTION_ID = "PAYMENT_METHOD_ID"
internal const val INSTRUMENT_ID = "INSTRUMENT_ID"

internal class ContractFragment : Fragment() {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: ContractVmFactory.AssistedContractVMFactory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var testParameters: TestParameters

    @Inject
    lateinit var reporter: Reporter

    private val viewModel: ContractViewModel by viewModel(ContractModule.CONTRACT) {
        viewModelFactory.create(
            paymentOptionId,
            instrumentId
        )
    }

    private val topBarAnimator: ViewPropertyAnimator? by lazy { binding.topBar.animate() }

    private val paymentOptionId: Int by lazy { requireNotNull(arguments?.getInt(PAYMENT_OPTION_ID)) }

    private val instrumentId: String? by lazy { arguments?.getString(INSTRUMENT_ID) }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    private var _binding: YmFragmentContractBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onAttach(context: Context) {
        CheckoutInjector.injectContractFragment(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = YmFragmentContractBinding.inflate(inflater, container, false)
        binding.nextButton.backgroundTintList = InMemoryColorSchemeRepository.backgroundStateList(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isTablet) {
            postponeEnterTransition()
        }
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setFragmentResultListener(TokenizeFragment.TOKENIZE_RESULT_KEY) { key, bundle ->
            val result =
                bundle.getSerializable(TokenizeFragment.TOKENIZE_RESULT_EXTRA) as Screen.Tokenize.TokenizeResult
            if (result == Screen.Tokenize.TokenizeResult.CANCEL) {
                viewModel.handleAction(Action.TokenizeCancelled)
            }
        }

        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {
                showError(it) {
                    viewModel.handleAction(Action.Load)
                }
            }
        )

        viewModel.handleAction(Action.Load)
    }

    private fun onBackPressed() {
        binding.contentView.hideSoftKeyboard()
        parentFragmentManager.popBackStack()
        router.navigateTo(Screen.PaymentOptions())
    }

    private fun setupView() {
        if (isTablet) {
            binding.rootContainer.updateLayoutParams<ViewGroup.LayoutParams> {
                height = resources.getDimensionPixelSize(R.dimen.ym_dialogHeight)
            }
        }

        binding.phoneInput.editText.configureForPhoneInput()
        binding.phoneInput.editText.setOnEditorActionListener { _, action, _ ->
            (action == EditorInfo.IME_ACTION_DONE).also {
                if (it) {
                    binding.nextButton.performClick()
                    binding.phoneInput.editText.hideSoftKeyboard()
                }
            }
        }
        binding.phoneInput.editText.addTextChangedListener(object : SimpleTextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.phoneInput.error = ""
                binding.phoneInput.hint = ""
                binding.nextButton.isEnabled = s.toString().isPhoneNumber
            }
        })
        binding.allowWalletLinking.onCheckedChangedListener {
            viewModel.handleAction(Action.ChangeAllowWalletLinking(it))
        }
        initOnScrollChangeListener()
        binding.bankCardView.setBankCardAnalyticsLogger(object : BankCardAnalyticsLogger {
            override fun onNewEvent(event: BankCardEvent) {
                reporter.report("actionBankCardForm", event.toString())
            }
        })
    }

    override fun onDestroyView() {
        view?.hideSoftKeyboard()
        super.onDestroyView()
        topBarAnimator?.cancel()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCAN_BANK_CARD && resultCode == Activity.RESULT_OK) {
            data?.extras?.apply {
                val cardNumber = getString(EXTRA_CARD_NUMBER)?.filter(Char::isDigit)
                val expiryMonth = getInt(EXTRA_EXPIRY_MONTH).takeIf { it > 0 }
                val expiryYear = getInt(EXTRA_EXPIRY_YEAR).takeIf { it > 0 }?.rem(100)

                binding.bankCardView.setBankCardInfo(cardNumber, expiryYear, expiryMonth)
            }
        }
    }

    private fun showState(state: State) {
        showState(!isTablet) {
            when (state) {
                State.Loading -> showLoadingState()
                is State.Content -> showContentState(state)
                is State.Error -> showErrorState(state.error) {
                    viewModel.handleAction(Action.Load)
                }
            }
        }
    }

    private fun showState(withAnimation: Boolean, changeView: () -> Unit) {
        if (withAnimation) {
            changeViewWithAnimation(binding.rootContainer, changeView)
        } else {
            changeView()
        }
    }

    private fun showLoadingState() {
        binding.rootContainer.showChild(binding.loadingView)
    }

    private fun showContentState(content: State.Content) {
        binding.rootContainer.showChild(binding.contentView)
        if (content.isSinglePaymentMethod) {
            onBackPressedCallback.remove()
            binding.topBar.backButton.hide()
        } else {
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
            binding.topBar.onBackButton { onBackPressed() }
        }
        binding.title.text = content.shopTitle
        binding.subtitle.text = content.shopSubtitle

        val paymentOption = content.contractInfo.paymentOption
        binding.topBar.title = paymentOption.title ?: paymentOption.getPlaceholderTitle(requireContext())
        binding.sum.text = paymentOption.charge.format().makeStartBold()
        setUpFee(paymentOption.fee)
        binding.nextButton.setOnClickListener {
            view?.hideSoftKeyboard()
            viewModel.handleAction(Action.Tokenize())
        }

        val savePaymentMethodOption = content.getSavePaymentMethodOption()
        setUpSavePaymentMethodOption(content, savePaymentMethodOption)

        binding.nextButton.isEnabled = isNextButtonEnabled(content.contractInfo.paymentOption)
        binding.switches.visible =
            content.confirmation !is MobileApplication || (content.savePaymentMethod != SavePaymentMethod.OFF)
        showContractInfo(content, content.contractInfo)
        resumePostponedTransition(binding.rootContainer)
        binding.loadingView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = binding.rootContainer.getViewHeight()
        }
    }

    private fun showContractInfo(content: State.Content, contractInfo: ContractInfo) {
        when (contractInfo) {
            is ContractInfo.WalletContractInfo -> {
                setUpWalletLinking(contractInfo.showAllowWalletLinking)
                setUpAuthorizedWallet(contractInfo, contractInfo.paymentOption)
                binding.switches.visible = contractInfo.showAllowWalletLinking
            }

            is ContractInfo.WalletLinkedCardContractInfo -> {
                setUpWalletLinking(contractInfo.showAllowWalletLinking)
                binding.allowWalletLinking.isChecked = contractInfo.allowWalletLinking
                setUpLinkedBankCardView(
                    contractInfo.paymentOption.pan.replace("*", "•"),
                    contractInfo.paymentOption.brand
                )
                binding.switches.visible = contractInfo.showAllowWalletLinking
            }

            is ContractInfo.PaymentIdCscConfirmationContractInfo -> {
                val first = contractInfo.paymentOption.first
                val last = contractInfo.paymentOption.last
                val brand = contractInfo.paymentOption.brand
                binding.allowWalletLinking.isChecked = contractInfo.allowWalletLinking
                setUpLinkedBankCardView(("$first••••••$last"), brand)
            }

            is ContractInfo.LinkedBankCardContractInfo -> setUpBankCardView(contractInfo.instrument)
            is ContractInfo.NewBankCardContractInfo -> setUpBankCardView(null)
            is ContractInfo.SberBankContractInfo -> setUpSberbankView(content, contractInfo.userPhoneNumber)
            is ContractInfo.AbstractWalletContractInfo -> setUpAbstractWallet()
            is ContractInfo.SBPContractInfo -> setUpSBPView()
        }

        binding.licenseAgreement.apply {
            text = getTransactionAgreementText(content)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setUpCardFormWithCsc(instrumentBankCard: PaymentInstrumentBankCard) {
        with(binding.bankCardView) {
            presetBankCardInfo(instrumentBankCard.cardNumber)
            showBankLogo(getBankOrBrandLogo(instrumentBankCard.cardNumber, instrumentBankCard.cardType))
            setOnPresetBankCardReadyListener { cvc ->
                binding.nextButton.isEnabled = true
                binding.nextButton.setOnClickListener {
                    viewModel.handleAction(Action.TokenizePaymentInstrument(instrumentBankCard, cvc))
                }
                view?.hideSoftKeyboard()
            }
        }
    }

    private fun setUpCardFormWithoutCsc(instrumentBankCard: PaymentInstrumentBankCard) {
        with(binding.bankCardView) {
            setCardData(instrumentBankCard.cardNumber)
            setChangeCardAvailable(false)
            hideAdditionalInfo()
            showBankLogo(getBankOrBrandLogo(instrumentBankCard.cardNumber, instrumentBankCard.cardType))
        }
        binding.nextButton.isEnabled = true
        binding.nextButton.setOnClickListener {
            viewModel.handleAction(Action.TokenizePaymentInstrument(instrumentBankCard, null))
        }
    }

    private fun setUpFee(fee: Fee?) {
        val feeString = fee.formatService()
        if (feeString != null) {
            binding.feeLayout.visible = true
            binding.feeView.text = feeString
        } else {
            binding.feeLayout.visible = false
        }
    }

    private fun setUpWalletLinking(showAllowWalletLinking: Boolean) {
        binding.allowWalletLinking.visible = showAllowWalletLinking
        binding.allowWalletLinking.title = getString(R.string.ym_contract_link_wallet_title)
        binding.allowWalletLinking.description =
            binding.allowWalletLinking.context.getString(R.string.ym_allow_wallet_linking)
    }

    private fun setUpSavePaymentMethodOption(content: State.Content, option: SavePaymentMethodOption) {
        when (option) {
            is SavePaymentMethodOption.SwitchSavePaymentMethodOption -> setUpSwitchSavePaymentMethodOption(
                option,
                content.shouldSavePaymentMethod || content.shouldSavePaymentInstrument
            )

            is SavePaymentMethodOption.MessageSavePaymentMethodOption -> setUpMessageSavePaymentMethodOption(option)
            is SavePaymentMethodOption.None -> {
                binding.savePaymentMethodMessageTitle.visible = false
                binding.savePaymentMethodMessageSubTitle.visible = false
                binding.savePaymentMethodSelection.visible = false
            }
        }
    }

    private fun setUpSwitchSavePaymentMethodOption(
        savePaymentMethodOption: SavePaymentMethodOption.SwitchSavePaymentMethodOption,
        checked: Boolean,
    ) {
        binding.savePaymentMethodSelection.apply {
            title = savePaymentMethodOption.title
            with(findViewById<TextView>(R.id.descriptionView)) {
                movementMethod = LinkMovementMethod.getInstance()
                text = formatHtmlWithLinks(savePaymentMethodOption.subtitle) {
                    ContextCompat.startActivity(
                        requireContext(),
                        SavePaymentMethodInfoActivity.create(
                            requireContext(),
                            savePaymentMethodOption.screenTitle,
                            savePaymentMethodOption.screenText
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        null
                    )
                }
            }
            onCheckedChangedListener(null)
            isChecked = checked
            onCheckedChangedListener { savePaymentMethod ->
                viewModel.handleAction(Action.ChangeSavePaymentMethod(savePaymentMethod))
            }
        }
        binding.savePaymentMethodMessageTitle.visible = false
        binding.savePaymentMethodMessageSubTitle.visible = false
        binding.savePaymentMethodSelection.visible = true
    }

    private fun setUpMessageSavePaymentMethodOption(option: SavePaymentMethodOption.MessageSavePaymentMethodOption) {
        binding.savePaymentMethodMessageTitle.text = option.title
        binding.savePaymentMethodMessageSubTitle.apply {
            text = formatHtmlWithLinks(option.subtitle) {
                ContextCompat.startActivity(
                    requireContext(),
                    SavePaymentMethodInfoActivity.create(
                        requireContext(),
                        option.screenTitle,
                        option.screenText
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    null
                )
            }
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.savePaymentMethodMessageTitle.visible = true
        binding.savePaymentMethodMessageSubTitle.visible = true
        binding.savePaymentMethodSelection.visible = false
    }

    private fun showErrorState(error: Throwable, action: () -> Unit) {
        when (error) {
            is ApiMethodException -> handleApiException(error, action)

            else -> showError(error, action)
        }
        resumePostponedTransition(binding.rootContainer)
    }

    private fun handleApiException(error: ApiMethodException, action: () -> Unit) {
        when (error.error.errorCode) {
            ErrorCode.INVALID_TOKEN -> router.navigateTo(Screen.PaymentOptions())
            ErrorCode.INVALID_CONTEXT, ErrorCode.CREATE_TIMEOUT_NOT_EXPIRED, ErrorCode.SESSIONS_EXCEEDED -> router.navigateTo(
                Screen.PaymentOptions()
            )

            else -> showError(error, action)
        }
    }

    private fun showError(throwable: Throwable, action: () -> Unit) {
        binding.composeContainerError.apply {
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
                        action()
                    }
                }
            }
        }
        binding.rootContainer.showChild(binding.composeContainerError)
        binding.loadingView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = binding.rootContainer.getViewHeight()
        }
    }

    private fun showEffect(effect: Effect) {
        when (effect) {
            is Effect.ShowTokenize -> router.navigateTo(Screen.Tokenize(effect.tokenizeInputModel))
            Effect.RestartProcess -> {
                parentFragmentManager.popBackStack()
                router.navigateTo(Screen.PaymentOptions())
            }

            Effect.CancelProcess -> router.navigateTo(Screen.TokenizeCancelled)
        }
    }

    private fun setUpAuthorizedWallet(contractInfo: ContractInfo.WalletContractInfo, wallet: Wallet) {
        binding.yooMoneyAccountView.root.show()
        binding.yooMoneyAccountView.yooSubtitle.show()
        binding.yooMoneyAccountView.yooAction.show()

        binding.yooMoneyAccountView.yooTitle.text = contractInfo.walletUserAuthName ?: wallet.walletId
        binding.yooMoneyAccountView.yooSubtitle.text = wallet.balance?.format()

        binding.allowWalletLinking.isChecked = contractInfo.allowWalletLinking

        contractInfo.walletUserAvatarUrl?.let {
            Picasso.get().load(Uri.parse(it))
                .placeholder(R.drawable.ym_user_avatar)
                .cropToCircle()
                .into(binding.yooMoneyAccountView.yooImage)
        } ?: binding.yooMoneyAccountView.yooImage.setImageResource(R.drawable.ym_user_avatar)

        binding.yooMoneyAccountView.yooAction.setOnClickListener {
            val dialogContent = YmAlertDialog.DialogContent(
                title = getString(
                    R.string.ym_logout_dialog_message,
                    contractInfo.walletUserAuthName ?: wallet.walletId
                ),
                content = null,
                actionPositiveText = getString(R.string.ym_logout_dialog_button_positive),
                actionNegativeText = getString(R.string.ym_logout_dialog_button_negative),
                isPositiveActionAlert = true
            )

            CheckoutAlertDialog.create(childFragmentManager, content = dialogContent, dimAmount = 0.6f).apply {
                attachListener(object : YmAlertDialog.DialogListener {
                    override fun onPositiveClick() {
                        viewModel.handleAction(Action.Logout)
                    }
                })
            }.show(childFragmentManager)
        }
    }

    private fun setUpAbstractWallet() {
        router.navigateTo(Screen.MoneyAuth)
    }

    private fun setUpSBPView() {
        binding.sbpView.root.show()
        binding.nextButton.setOnClickListener {
            view?.hideSoftKeyboard()
            binding.rootContainer.showChild(binding.loadingView)
            viewModel.handleAction(Action.Tokenize(SBPInfo))
        }
    }

    private fun setUpBankCardView(instrumentBankCard: PaymentInstrumentBankCard?) {
        with(binding.bankCardView) {
            show()
            if (instrumentBankCard != null) {
                if (instrumentBankCard.cscRequired) {
                    setUpCardFormWithCsc(instrumentBankCard)
                } else {
                    setUpCardFormWithoutCsc(instrumentBankCard)
                }
            } else {
                setOnBankCardReadyListener { cardInfo ->
                    binding.nextButton.isEnabled = true
                    binding.nextButton.setOnClickListener {
                        viewModel.handleAction(Action.Tokenize(cardInfo))
                    }
                    view?.hideSoftKeyboard()
                }
                setOnBankCardScanListener {
                    startActivityForResult(it, REQUEST_CODE_SCAN_BANK_CARD)
                }
            }
            setOnBankCardNotReadyListener { binding.nextButton.isEnabled = false }

            setOnBankCardScanListener {
                startActivityForResult(
                    it,
                    REQUEST_CODE_SCAN_BANK_CARD
                )
            }
        }
    }

    private fun setUpLinkedBankCardView(cardNumber: String, brand: CardBrand) {
        with(binding.bankCardView) {
            show()
            presetBankCardInfo(cardNumber)
            binding.bankCardView.showBankLogo(getBankOrBrandLogo(cardNumber, brand))

            setOnPresetBankCardReadyListener { cvc ->
                binding.nextButton.isEnabled = true
                binding.nextButton.setOnClickListener {
                    viewModel.handleAction(Action.Tokenize(LinkedCardInfo(cvc)))
                }
                view?.hideSoftKeyboard()
            }

            setOnBankCardNotReadyListener { binding.nextButton.isEnabled = false }
        }
    }

    private fun setUpSberbankView(content: State.Content, userPhoneNumber: String?) {
        if (userPhoneNumber != null) {
            binding.phoneInput.text = userPhoneNumber
        }
        if (content.confirmation is MobileApplication) {
            binding.sberPayView.root.show()
            binding.nextButton.isEnabled = true
            binding.nextButton.setOnClickListener {
                viewModel.handleAction(Action.Tokenize(SberPay))
            }
        } else {
            binding.nextButton.isEnabled = binding.phoneInput.text?.isPhoneNumber ?: false
            binding.phoneInputContainer.show()

            binding.nextButton.setOnClickListener { _ ->
                view?.hideSoftKeyboard()
                val text = binding.phoneInput.text
                if (text != null && text.isPhoneNumber) {
                    viewModel.handleAction(Action.Tokenize(SbolSmsInvoicingInfo(text.toString())))
                } else {
                    binding.phoneInput.error = " "
                }
            }
        }
    }

    private fun isNextButtonEnabled(paymentOption: PaymentOption): Boolean {
        return when (paymentOption) {
            is BankCardPaymentOption, is LinkedCard, is PaymentIdCscConfirmation -> false
            else -> true
        }
    }

    private fun getLicenseAgreementText(content: State.Content): CharSequence {
        return formatHtmlWithLinks(
            content.userAgreementUrl ?: getDefaultAgentSchemeUserAgreementUrl(requireContext())
        ) {
            ContextCompat.startActivity(
                requireContext(),
                WebViewActivity.create(
                    requireContext(),
                    it,
                    null,
                    testParameters
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
            )
        }
    }

    private fun getTransactionAgreementText(content: State.Content): CharSequence {
        val result = SpannableStringBuilder()
        result.append(getLicenseAgreementText(content))
        if (content.isSplitPayment) {
            result.append("\n")
            result.append(getSafeTransactionAgreementText())
        }
        return result
    }

    private fun getSafeTransactionAgreementText() = getMessageWithLink(
        requireContext(),
        R.string.ym_safe_payments_agreement_part_1,
        R.string.ym_safe_payments_agreement_part_2
    ) {
        ContextCompat.startActivity(
            requireContext(),
            SavePaymentMethodInfoActivity.create(
                requireContext(),
                R.string.ym_safe_payments_agreement_title,
                R.string.ym_safe_payments_agreement_message,
                null
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            null
        )
    }

    private fun initOnScrollChangeListener() {
        val elevation = requireContext().resources.getDimension(ru.yoomoney.sdk.gui.gui.R.dimen.ym_elevationXS)
        binding.contractScrollView.viewTreeObserver.addOnScrollChangedListener {
            runCatching {
                topBarAnimator
                    ?.takeIf { binding.contractScrollView?.scrollY != null }
                    ?.translationZ(if (binding.contractScrollView.scrollY > 0) elevation else .0f)
                    ?.start()
            }
        }
    }

    internal companion object {
        fun newInstance(paymentOptionId: Int, instrumentId: String?): Fragment {
            return ContractFragment().apply {
                arguments = bundleOf(
                    PAYMENT_OPTION_ID to paymentOptionId,
                    INSTRUMENT_ID to instrumentId
                )
            }
        }
    }
}

private fun CharSequence.makeStartBold() =
    (this as? Spannable ?: SpannableStringBuilder(this)).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
