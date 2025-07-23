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

package ru.yoomoney.sdk.kassa.payments.di.component

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigModule
import ru.yoomoney.sdk.kassa.payments.di.module.CoreModule
import ru.yoomoney.sdk.kassa.payments.di.module.CurrentUserModule
import ru.yoomoney.sdk.kassa.payments.di.module.LoadedOptionModule
import ru.yoomoney.sdk.kassa.payments.di.module.OkHttpModule
import ru.yoomoney.sdk.kassa.payments.di.module.ReporterModule
import ru.yoomoney.sdk.kassa.payments.di.module.SharedPreferencesModule
import ru.yoomoney.sdk.kassa.payments.di.module.TokenStorageModule
import ru.yoomoney.sdk.kassa.payments.di.module.TokensStorageModule
import ru.yoomoney.sdk.kassa.payments.di.module.ViewModelKeyedFactoryModule
import ru.yoomoney.sdk.kassa.payments.di.module.AppMetricaReporterModule
import ru.yoomoney.sdk.kassa.payments.di.scope.MainScope
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import javax.inject.Singleton

@MainScope
@Singleton
@Component(
    modules = [
        ViewModelKeyedFactoryModule::class,
        SharedPreferencesModule::class,
        LoadedOptionModule::class,
        ReporterModule::class,
        OkHttpModule::class,
        CoreModule::class,
        ConfigModule::class,
        TokenStorageModule::class,
        TokensStorageModule::class,
        AppMetricaReporterModule::class,
        CurrentUserModule::class,
    ]
)
internal interface MainComponent {

    fun inject(mainDialogFragment: MainDialogFragment)

    fun appendCheckoutSubcomponent() : CheckoutSubcomponent.Builder

    fun appendConfirmationSubcomponent(): ConfirmationSubcomponent.Builder

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun testParameters(testParameters: TestParameters): Builder

        @BindsInstance
        fun clientApplicationKey(clientApplicationKey: String): Builder

        fun okHttpModule(okHttpModule: OkHttpModule): Builder

        fun tokensStorageModule(tokensStorageModule: TokensStorageModule): Builder

        fun mainReporterModule(appMetricaReporterModule: AppMetricaReporterModule): Builder

        fun build(): MainComponent
    }
}