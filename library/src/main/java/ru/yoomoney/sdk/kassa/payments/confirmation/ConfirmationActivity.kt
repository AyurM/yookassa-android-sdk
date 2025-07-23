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

package ru.yoomoney.sdk.kassa.payments.confirmation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.yoomoney.sdk.kassa.payments.Checkout.RESULT_ERROR
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.confirmation.sberpay.SberPaySdkManager
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.metrics.RecreationParam
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SberPayConfirmationStatusSuccess
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.SBP_CONFIRMATION_ACTION
import ru.yoomoney.sdk.kassa.payments.threeDS.EXTRA_TEST_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.TAG_BOTTOM_SHEET
import ru.yoomoney.sdk.kassa.payments.ui.color.ColorScheme
import ru.yoomoney.sdk.kassa.payments.ui.model.StartScreenData
import ru.yoomoney.sdk.kassa.payments.utils.INVOICING_AUTHORITY
import ru.yoomoney.sdk.kassa.payments.utils.SBERPAY_PATH
import ru.yoomoney.sdk.kassa.payments.utils.SBP_PATH
import javax.inject.Inject

internal const val EXTRA_PAYMENT_METHOD_TYPE = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_METHOD_TYPE"
internal const val EXTRA_CONFIRMATION_URL = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_CONFIRMATION_URL"
internal const val EXTRA_CLIENT_APPLICATION_KEY = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_CLIENT_APPLICATION_KEY"
internal const val EXTRA_SHOP_ID = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_SHOP_ID"
internal const val EXTRA_COLOR_SCHEME = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_COLOR_SCHEME"

private const val SBER_PAY_CONFIRMATION_ACTION = "actionSberPayConfirmation"

internal class ConfirmationActivity : AppCompatActivity() {

    private val paymentMethodType: PaymentMethodType by lazy {
        intent.getSerializableExtra(EXTRA_PAYMENT_METHOD_TYPE) as PaymentMethodType
    }
    private val confirmationUrl: String? by lazy { intent.getStringExtra(EXTRA_CONFIRMATION_URL) }
    private val clientApplicationKey: String? by lazy { intent.getStringExtra(EXTRA_CLIENT_APPLICATION_KEY) }
    private val shopId: String? by lazy { intent.getStringExtra(EXTRA_SHOP_ID) }
    private val testParameters: TestParameters by lazy {
        intent.getParcelableExtra(EXTRA_TEST_PARAMETERS) as TestParameters? ?: TestParameters()
    }
    private val colorScheme: ColorScheme? by lazy {
        intent.getParcelableExtra(EXTRA_COLOR_SCHEME)
    }
    private var isWaitingSberForResult = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @Inject
    lateinit var reporter: Reporter

    override fun onCreate(savedInstanceState: Bundle?) {
        if (clientApplicationKey == null || colorScheme == null) {
            super.onCreate(savedInstanceState)
            setResult(RESULT_ERROR)
            finish()
            return
        }
        SberPaySdkManager.init(application)
        CheckoutInjector.setupComponent(
            isConfirmation = true,
            context = this,
            clientApplicationKey = clientApplicationKey,
            testParameters = testParameters,
            uiParameters = UiParameters(colorScheme = requireNotNull(colorScheme))
        )
        CheckoutInjector.injectConfirmationActivity(this)
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            startConfirmationProcess(paymentMethodType)
        } else {
            reporter.report(RecreationParam.RECREATION_EVENT_NAME, RecreationParam.CONFIRMATION_ACTIVITY_PARAM)
        }
    }

    override fun onResume() {
        super.onResume()
        if (paymentMethodType == PaymentMethodType.SBERBANK) {
            if (isWaitingSberForResult) {
                isWaitingSberForResult = false
                setResult(Activity.RESULT_CANCELED)
                finish()
            } else {
                isWaitingSberForResult = true
            }
        }
    }

    private fun startConfirmationProcess(
        paymentMethodType: PaymentMethodType?,
    ) {
        if (confirmationUrl == null || shopId == null) {
            setResult(RESULT_ERROR)
            finish()
        }
        when (paymentMethodType) {
            PaymentMethodType.SBERBANK -> {
                val startScreenData = StartScreenData.SberPayConfirmationData(
                    requireNotNull(confirmationUrl),
                    requireNotNull(shopId)
                )
                showDialog(startScreenData)
            }

            PaymentMethodType.SBP -> {
                val startScreenData = StartScreenData.SBPConfirmationData(requireNotNull(confirmationUrl))
                showDialog(startScreenData)
            }

            else -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onDestroy() {
        detachMainDialogFragment()
        super.onDestroy()
    }

    private fun detachMainDialogFragment() {
        findDialog(supportFragmentManager)?.dialog?.apply {
            setOnCancelListener(null)
            setOnDismissListener(null)
        }
    }

    private fun showDialog(startScreenData: StartScreenData) {
        findDialog(supportFragmentManager) ?: MainDialogFragment.createFragment(startScreenData)
            .show(supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    private fun findDialog(supportFragmentManager: FragmentManager) =
        supportFragmentManager.findFragmentByTag(TAG_BOTTOM_SHEET) as MainDialogFragment?

    private fun handleIntent(newIntent: Intent) {
        val sberPayResult =
            newIntent.data?.authority == INVOICING_AUTHORITY && newIntent.data?.path?.contains(SBERPAY_PATH) == true
        val sbpResult = newIntent.data?.authority == SBP_PATH
        when {
            sberPayResult -> {
                isWaitingSberForResult = false
                setResult(Activity.RESULT_OK)
                reporter.report(SBER_PAY_CONFIRMATION_ACTION, listOf(SberPayConfirmationStatusSuccess()))
            }

            sbpResult -> {
                setResult(Activity.RESULT_OK)
                reporter.report(SBP_CONFIRMATION_ACTION)
            }

            else -> setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    companion object {
        fun createIntent(
            context: Context, confirmationUrl: String,
            paymentMethodType: PaymentMethodType,
            clientApplicationKey: String,
            shopId: String,
            testParameters: TestParameters = TestParameters(),
            colorScheme: ColorScheme,
        ): Intent = Intent(context, ConfirmationActivity::class.java)
            .putExtra(EXTRA_CONFIRMATION_URL, confirmationUrl)
            .putExtra(EXTRA_PAYMENT_METHOD_TYPE, paymentMethodType)
            .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
            .putExtra(EXTRA_SHOP_ID, shopId)
            .putExtra(EXTRA_CLIENT_APPLICATION_KEY, clientApplicationKey)
            .putExtra(EXTRA_COLOR_SCHEME, colorScheme)
    }
}
