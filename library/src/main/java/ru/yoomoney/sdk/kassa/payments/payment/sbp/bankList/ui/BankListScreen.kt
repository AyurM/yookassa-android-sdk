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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import ru.yoomoney.sdk.guiCompose.theme.YooTheme
import ru.yoomoney.sdk.guiCompose.views.emptystates.EmptyStateLargeView
import ru.yoomoney.sdk.guiCompose.views.form.TextInputView
import ru.yoomoney.sdk.guiCompose.views.listItems.action.ItemImageRoundActionView
import ru.yoomoney.sdk.guiCompose.views.shimmer.shimmerBrush
import ru.yoomoney.sdk.guiCompose.views.topbar.TopBarDefault
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModel
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpWidgetBankDomain
import ru.yoomoney.sdk.kassa.payments.ui.compose.CustomDimens
import ru.yoomoney.sdk.kassa.payments.ui.compose.ErrorStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.LoadingStateScreen
import ru.yoomoney.sdk.kassa.payments.ui.compose.MoneyPaymentComposeContent
import ru.yoomoney.sdk.kassa.payments.utils.compose.roundBottomSheetCorners
import ru.yoomoney.sdk.gui.gui.R as GuiResources

@Composable
internal fun BankListScreen(
    state: BankListUiState,
    errorFormatter: ErrorFormatter,
    viewModel: BankListViewModel,
) {
    val localKeyboardController = LocalSoftwareKeyboardController.current
    val onClickBank: (url: String) -> Unit = { viewModel.handleAction(BankList.Action.SelectBank(it)) }

    Box(
        modifier = Modifier
            .animateContentSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .roundBottomSheetCorners()
    ) {
        when (state) {
            is BankListUiState.Progress,
            is BankListUiState.BankListStatusProgress,
            -> LoadingScreen()

            is BankListUiState.Error -> ShowError(
                subTitle = errorFormatter.format(state.throwable).toString(),
                onClickRetry = { viewModel.handleAction(BankList.Action.LoadBankList) }
            )

            is BankListUiState.ActivityNotFoundError -> ShowError(
                subTitle = errorFormatter.format(state.throwable).toString(),
                buttonText = LocalContext.current.getString(R.string.ym_understand_button),
                onClickRetry = { viewModel.handleAction(BankList.Action.BackToBankList) }
            )

            is BankListUiState.BankListContent -> {
                ShowBankListContent(
                    state,
                    onClickBank,
                    onTextChange = { viewModel.handleAction(BankList.Action.Search(it)) }
                )
            }

            is BankListUiState.PaymentBankListStatusError -> ShowError(
                subTitle = errorFormatter.format(state.throwable).toString(),
                onClickRetry = { viewModel.handleAction(BankList.Action.LoadPaymentStatus) }
            )
        }
    }
    BackHandler {
        viewModel.handleAction(BankList.Action.Search(""))
        localKeyboardController?.hide()
        viewModel.handleAction(BankList.Action.BackToBankList)
    }
}

@Composable
internal fun ShowError(
    subTitle: String,
    buttonText: String = LocalContext.current.getString(R.string.ym_retry),
    onClickRetry: () -> Unit,
) {
    ErrorStateScreen(
        modifier = Modifier
            .fillMaxWidth()
            .height(CustomDimens.bottomDialogMaxHeight),
        action = buttonText,
        subtitle = subTitle,
        onClick = onClickRetry
    )
}

@Composable
private fun ShowBankListContent(
    bankListContent: BankListUiState.BankListContent,
    onClickBank: (deeplink: String) -> Unit,
    onTextChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        TopBarDefault(
            title = stringResource(id = R.string.ym_sbp_select_bank_title),
            navigationPainter = null
        )
        val isSearchingNow = bankListContent.searchText.isEmpty().not()
        val bankList = if (isSearchingNow) {
            bankListContent.searchedBanks
        } else {
            bankListContent.bankList
        }

        var searchText by remember { mutableStateOf(bankListContent.searchText) }
        val focusManager = LocalFocusManager.current
        TextInputView(
            text = searchText,
            onTextChange = {
                searchText = it
                onTextChange(it)
            },
            hint = stringResource(id = R.string.ym_bank_list_sbp_search_hint),
            modifier = Modifier
                .padding(
                    start = YooTheme.dimens.spaceS,
                    end = YooTheme.dimens.spaceS
                ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )
        BankList(
            bankListEntities = mapToBankListViewEntities(bankList),
            onClickBank = onClickBank,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CustomDimens.bottomDialogMaxHeight), contentAlignment = Alignment.Center
    ) {
        LoadingStateScreen()
    }
}

@Composable
private fun BankList(
    modifier: Modifier = Modifier,
    bankListEntities: List<BankListViewEntity>,
    onClickBank: (deeplink: String) -> Unit,
) {
    val pxLogoValue = with(LocalDensity.current) { 40.dp.roundToPx() }
    BoxWithConstraints {
        val pageHeight = this.maxHeight
        LazyColumn(modifier = modifier) {
            items(bankListEntities) { item ->
                BankListItem(item, pxLogoValue, pageHeight, onClickBank)
            }
        }
    }

}

@Composable
private fun BankListItem(
    item: BankListViewEntity,
    pxLogoValue: Int,
    pageHeight: Dp,
    onClickBank: (deeplink: String) -> Unit,
) {
    when (item) {
        is BankListViewEntity.BankViewEntity -> BankItem(item, pxLogoValue, onClickBank)
        is BankListViewEntity.Divider -> DividerItem()
        is BankListViewEntity.EmptyState -> EmptyStateItem(pageHeight)
    }
}

@Composable
private fun BankItem(
    bankViewEntity: BankListViewEntity.BankViewEntity,
    pxLogoValue: Int,
    onClickBank: (deeplink: String) -> Unit,
) {
    ItemImageRoundActionView(
        title = bankViewEntity.title,
        painter = rememberAsyncImagePainter(
            model = bankViewEntity.logo,
            placeholder = BrushPainter(
                shimmerBrush(size = IntSize(pxLogoValue, pxLogoValue))
            )
        ),
        hasRightElement = false,
        onClick = { onClickBank(bankViewEntity.url) }
    )
}

@Composable
private fun DividerItem() {
    Divider(
        modifier = Modifier.padding(
            start = dimensionResource(GuiResources.dimen.ym_spaceM),
            end = dimensionResource(GuiResources.dimen.ym_spaceM)
        )
    )
}

@Composable
private fun EmptyStateItem(pageHeight: Dp) {
    EmptyStateLargeView(
        modifier = Modifier.height(pageHeight),
        title = stringResource(id = R.string.ym_title_not_found),
        subtitle = stringResource(id = R.string.ym_subtitle_sbp_not_found),
        painter = painterResource(id = R.drawable.ym_search_not_found),
    )
}

@Preview(showBackground = true)
@Composable
private fun BankListPreview() {
    MoneyPaymentComposeContent {
        ShowBankListContent(
            BankListUiState.BankListContent(
                listOf(
                    SbpWidgetBankDomain("bank 1", "", "123"),
                    SbpWidgetBankDomain("bank 1", "", "123"),
                    SbpWidgetBankDomain("bank 1", "", "123"),
                    SbpWidgetBankDomain("bank 1", "", "123"),
                    SbpWidgetBankDomain("bank 1", "", "123"),
                )
            ), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyBankListPreview() {
    MoneyPaymentComposeContent {
        ShowBankListContent(
            BankListUiState.BankListContent(emptyList()), {}, {}
        )
    }
}