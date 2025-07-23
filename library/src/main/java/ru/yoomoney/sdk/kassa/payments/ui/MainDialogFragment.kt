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

package ru.yoomoney.sdk.kassa.payments.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.gui.gui.R as GuiResources
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.metrics.SessionReporter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.ui.model.StartScreenData
import ru.yoomoney.sdk.kassa.payments.ui.view.BackPressedAppCompatDialog
import ru.yoomoney.sdk.kassa.payments.ui.view.BackPressedBottomSheetDialog
import ru.yoomoney.sdk.kassa.payments.ui.view.WithBackPressedListener
import javax.inject.Inject

internal class MainDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var sessionReporter: SessionReporter

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository

    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private val startScreenData: StartScreenData by lazy {
        requireNotNull(arguments?.getParcelable(START_SCREEN_DATA_INFO))
    }

    init {
        setStyle(STYLE_NO_FRAME, R.style.ym_MainDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        CheckoutInjector.injectMainDialogFragment(this)
        router.onAttach(this)
    }

    override fun onDetach() {
        super.onDetach()
        router.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = requireContext().let {
        if (isTablet) {
            BackPressedAppCompatDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
            }
        } else {
            BackPressedBottomSheetDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ym_fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isTablet) {
            view.findViewById<AppCompatImageView>(R.id.topArrowLine).hide()
        }
        router.initStartScreen(startScreenData, savedInstanceState)

        (dialog as? WithBackPressedListener)?.onBackPressed = {
            requireActivity().onBackPressed()
            true
        }

        dialog?.setOnShowListener {
            dialog?.findViewById<View>(GuiResources.id.design_bottom_sheet)?.apply {
                background = null
            }
        }

        // Look at https://jira.yooteam.ru/browse/MOC-4949
        // This code allows you to stretch the curtain to the height of the content
        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            (dialog as? BottomSheetDialog)?.also { dialog ->
                val bottomSheet = dialog.findViewById<FrameLayout?>(GuiResources.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                    peekHeight = 0
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            }
        }
        view.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    override fun onResume() {
        super.onResume()
        sessionReporter.resumeSession()
    }

    override fun onPause() {
        sessionReporter.pauseSession()
        super.onPause()
    }

    override fun onDestroyView() {

        (dialog as? WithBackPressedListener)?.onBackPressed = null

        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        view?.hideSoftKeyboard()
        super.onDismiss(dialog)
        if (isStateSaved.not()) {
            activity?.finish()
        }
    }

    companion object {
        private const val START_SCREEN_DATA_INFO = "ru.yoomoney.sdk.kassa.payments.ui.START_SCREEN_DATA_INFO"

        fun createFragment(startScreenData: StartScreenData): MainDialogFragment {
            return MainDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(START_SCREEN_DATA_INFO, startScreenData)
                }
            }
        }
    }
}
