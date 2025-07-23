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

package ru.yoomoney.sdk.kassa.payments.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.di.PaymentOptionsListFormatter
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.payment.CurrentUserRepository
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoomoney.sdk.kassa.payments.payment.unbindCard.UnbindCardGateway
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ConfigUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ConfigUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListErrorFormatter
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionsListUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.ShopPropertiesRepository
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCase
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.unbind.UnbindCardUseCaseImpl

@Module
internal class PaymentOptionsModule {

    @Provides
    @PaymentOptionsListFormatter
    fun errorFormatter(
        context: Context,
        errorFormatter: ErrorFormatter,
    ): ErrorFormatter {
        return PaymentOptionListErrorFormatter(context, errorFormatter)
    }


    @Provides
    fun paymentOptionsListUseCase(
        paymentParameters: PaymentParameters,
        paymentOptionListRepository: PaymentOptionListRepository,
        saveLoadedPaymentOptionsListRepository: SaveLoadedPaymentOptionsListRepository,
        paymentMethodInfoGateway: PaymentMethodInfoGateway,
        currentUserRepository: CurrentUserRepository,
        loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
        shopPropertiesRepository: ShopPropertiesRepository,
    ): PaymentOptionsListUseCase {
        return PaymentOptionsListUseCaseImpl(
            paymentOptionListRestrictions = paymentParameters.paymentMethodTypes,
            paymentOptionListRepository = paymentOptionListRepository,
            saveLoadedPaymentOptionsListRepository = saveLoadedPaymentOptionsListRepository,
            paymentMethodInfoGateway = paymentMethodInfoGateway,
            currentUserRepository = currentUserRepository,
            loadedPaymentOptionListRepository = loadedPaymentOptionListRepository,
            shopPropertiesRepository = shopPropertiesRepository
        )
    }

    @Provides
    fun unbindCardUseCaseProvider(
        unbindCardInfoGateway: UnbindCardGateway,
        getLoadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository,
    ): UnbindCardUseCase {
        return UnbindCardUseCaseImpl(unbindCardInfoGateway, getLoadedPaymentOptionListRepository)
    }

    @Provides
    fun configUseCase(configRepository: ConfigRepository): ConfigUseCase {
        return ConfigUseCaseImpl(configRepository)
    }

}