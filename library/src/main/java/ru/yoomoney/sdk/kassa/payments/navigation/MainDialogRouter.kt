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

package ru.yoomoney.sdk.kassa.payments.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.confirmation.EXTRA_PAYMENT_METHOD_TYPE
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPayConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.confirmation.sbp.ui.SBPConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.extensions.inTransaction
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.toType
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.ui.BankListFragment
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.ui.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.ui.BottomSheetFragmentTransition
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_PAYMENT_TOKEN
import ru.yoomoney.sdk.kassa.payments.ui.isTablet
import ru.yoomoney.sdk.kassa.payments.ui.model.StartScreenData
import ru.yoomoney.sdk.kassa.payments.unbind.ui.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.utils.isBuildDebug

private val TAG = MainDialogRouter::class.java.simpleName
internal const val PAYMENT_OPTION_LIST_FRAGMENT_TAG = "paymentOptionListFragment"
private const val CONTRACT_FRAGMENT_TAG = "contractFragment"
private const val TOKENIZE_FRAGMENT_TAG = "tokenizeFragment"
internal const val AUTH_FRAGMENT_TAG = "authFragment"
private const val PAYMENT_AUTH_FRAGMENT_TAG = "paymentAuthFragment"
private const val UNBIND_CARD_FRAGMENT_TAG = "unbindCardFragment"
internal const val CONFIRMATION_SBP_FRAGMENT_TAG = "confirmationSBPFragment"
internal const val CONFIRMATION_SBERPAY_FRAGMENT_TAG = "confirmationSberPayFragment"
private const val LIST_BANKS_FRAGMENT_TAG = "listBanksFragment"

internal class MainDialogRouter(
    private val context: Context,
    private val showLogs: Boolean,
) : Router {
    private var childFragmentManager: FragmentManager? = null
    private var activity: FragmentActivity? = null

    override fun onAttach(fragment: Fragment) {
        childFragmentManager = fragment.childFragmentManager
        activity = fragment.requireActivity()
    }

    override fun onDetach() {
        childFragmentManager = null
        activity = null
    }

    override fun navigateTo(screen: Screen) {
        if (isBuildDebug() && showLogs) {
            Log.d(TAG, "Navigating to $screen")
        }
        onScreenChanged(screen)
    }

    private fun onScreenChanged(screen: Screen) {
        val currentFragment = childFragmentManager?.fragments?.lastOrNull()
        when (screen) {
            is Screen.Contract -> transitToFragment(
                requireNotNull(currentFragment),
                ContractFragment.newInstance(screen.paymentOptionId, screen.instrumentId),
                CONTRACT_FRAGMENT_TAG
            )

            is Screen.PaymentOptions -> {
                childFragmentManager?.let { fragmentManager ->
                    val paymentOptionListFragment =
                        (fragmentManager.findFragmentByTag(PAYMENT_OPTION_LIST_FRAGMENT_TAG) as PaymentOptionListFragment)

                    if (paymentOptionListFragment.isHidden) {
                        fragmentManager.takeIf { !fragmentManager.isStateSaved && paymentOptionListFragment.isAdded }
                            ?.popBackStack()
                    }

                    paymentOptionListFragment.onAppear()
                }
            }

            is Screen.MoneyAuth -> {
                (childFragmentManager?.findFragmentByTag(AUTH_FRAGMENT_TAG) as? MoneyAuthFragment)?.authorize()
            }

            is Screen.Tokenize -> transitToFragment(
                requireNotNull(currentFragment),
                TokenizeFragment.newInstance(screen.tokenizeInputModel),
                TOKENIZE_FRAGMENT_TAG
            )

            is Screen.TokenizeSuccessful -> {
                val result = Intent()
                    .putExtra(EXTRA_PAYMENT_TOKEN, screen.tokenOutputModel.token)
                    .putExtra(EXTRA_PAYMENT_METHOD_TYPE, screen.tokenOutputModel.option.toType())

                activity?.let { fragmentActivity ->
                    fragmentActivity.setResult(Activity.RESULT_OK, result)
                    fragmentActivity.finish()
                }
            }

            is Screen.TokenizeCancelled -> activity?.let { fragmentActivity ->
                fragmentActivity.setResult(Activity.RESULT_CANCELED)
                fragmentActivity.finish()
            }

            is Screen.PaymentAuth -> transitToFragment(
                requireNotNull(currentFragment),
                PaymentAuthFragment.createFragment(screen.amount, screen.linkWalletToApp),
                PAYMENT_AUTH_FRAGMENT_TAG
            )

            is Screen.UnbindLinkedCard -> transitToFragment(
                requireNotNull(currentFragment),
                UnbindCardFragment.createFragment(screen.paymentOption as LinkedCard),
                UNBIND_CARD_FRAGMENT_TAG
            )

            is Screen.UnbindInstrument -> transitToFragment(
                requireNotNull(currentFragment),
                UnbindCardFragment.createFragment(screen.instrumentBankCard),
                UNBIND_CARD_FRAGMENT_TAG
            )
            // confirmation screens
            is Screen.SBPConfirmation -> transitToFragment(
                requireNotNull(currentFragment),
                SBPConfirmationFragment.createFragment(screen.confirmationData),
                CONFIRMATION_SBP_FRAGMENT_TAG
            )

            is Screen.SBPConfirmationSuccessful, Screen.SberPaySdkConfirmationSuccessful -> activity?.let { fragmentActivity ->
                fragmentActivity.setResult(Activity.RESULT_OK)
                fragmentActivity.finish()
            }

            is Screen.SberPaySdkConfirmationFailed -> activity?.let { fragmentActivity ->
                fragmentActivity.setResult(
                    Checkout.RESULT_ERROR,
                    Intent()
                        .putExtra(Checkout.EXTRA_ERROR_CODE, Checkout.ERROR_NOT_HTTPS_URL)
                        .putExtra(Checkout.EXTRA_ERROR_DESCRIPTION, screen.errorDescription)
                )
                fragmentActivity.finish()
            }

            is Screen.BankList -> transitToFragment(
                requireNotNull(currentFragment),
                BankListFragment.newInstance(screen.confirmationUrl, screen.paymentId),
                LIST_BANKS_FRAGMENT_TAG
            )
        }
    }

    override fun initStartScreen(startScreenData: StartScreenData, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            return
        }
        when {
            startScreenData is StartScreenData.BaseScreenData -> {
                inflateBaseFragment(
                    startScreenData
                )
            }

            startScreenData is StartScreenData.SBPConfirmationData
                    && startScreenData.confirmationData.isNotEmpty() -> {
                inflateSBPConfirmationFragment(
                    startScreenData.confirmationData
                )
            }

            startScreenData is StartScreenData.SberPayConfirmationData
                    && startScreenData.confirmationData.isNotEmpty() -> {
                inflateSberPayConfirmationFragment(
                    startScreenData.confirmationData,
                    startScreenData.shopId
                )
            }
        }
    }

    private fun inflateSBPConfirmationFragment(confirmationData: String) {
        childFragmentManager?.inTransaction {
            add(
                R.id.containerBottomSheet,
                SBPConfirmationFragment.createFragment(confirmationData),
                CONFIRMATION_SBP_FRAGMENT_TAG
            )
        }
    }

    private fun inflateSberPayConfirmationFragment(confirmationData: String, shopId: String) {
        childFragmentManager?.inTransaction {
            add(
                R.id.containerBottomSheet,
                SberPayConfirmationFragment.createFragment(confirmationData, shopId),
                CONFIRMATION_SBERPAY_FRAGMENT_TAG
            )
        }
    }

    private fun inflateBaseFragment(baseScreenData: StartScreenData.BaseScreenData) {
        childFragmentManager?.inTransaction {
            if (
                baseScreenData.paymentMethodType.contains(PaymentMethodType.YOO_MONEY) ||
                baseScreenData.paymentMethodType.isEmpty()
            ) {
                add(
                    R.id.authContainer,
                    MoneyAuthFragment(), AUTH_FRAGMENT_TAG
                )
            }
            add(
                R.id.containerBottomSheet,
                PaymentOptionListFragment.create(baseScreenData.paymentMethodId),
                PAYMENT_OPTION_LIST_FRAGMENT_TAG
            )
        }
    }

    private fun transitToFragment(fromFragment: Fragment, newFragment: Fragment, tag: String) {
        val currentFragmentRoot = fromFragment.requireView()
        childFragmentManager
            ?.beginTransaction()
            ?.apply {
                if (!fromFragment.requireContext().isTablet && currentFragmentRoot.transitionName != null) {
                    addSharedElement(currentFragmentRoot, currentFragmentRoot.transitionName)
                    setReorderingAllowed(true)
                    newFragment.sharedElementEnterTransition = BottomSheetFragmentTransition()
                }
            }
            ?.replace(R.id.containerBottomSheet, newFragment, tag)
            ?.addToBackStack(newFragment::class.java.name)
            ?.commit()
    }
}