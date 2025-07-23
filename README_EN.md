# <a name="yoomoney-payments-sdk"></a> YooMoney Payments SDK

[![Platform](https://img.shields.io/badge/Support-SDK%2021+-brightgreen.svg)](https://img.shields.io/badge/Support-SDK%2021+-brightgreen.svg)
[![Version](https://img.shields.io/maven-central/v/ru.yoomoney.sdk.kassa.payments/yookassa-android-sdk)](https://img.shields.io/maven-central/v/ru.yoomoney.sdk.kassa.payments/yookassa-android-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


This SDK allows processing payments using a payment token. It **works as an addition to the [YooMoney API](https://yookassa.ru/developers/payments/sdk-tokens)**.

The general payment process in the app is as follows:
- You start the process of [tokenization in the SDK](#tokenization);
- The user selects the payment method and enters the required data;
- The SDK exchanges this data for a payment token (`payment_token`) and returns it to you;
- After you receive `payment_token`, create a payment using the [API method](https://yookassa.ru/developers/api#create_payment);
- If necessary, launch the [payment confirmation](#payment-confirmation) process in the SDK using 3DS or SberPay.

**Tokenization and payment processes diagram:**

![Diagram](assets/images/diagrams/base_flow_diagram_en.png)

The SDK includes ready-made payment interfaces (payment form and everything related to it).

Using the SDK, you can receive tokens for processing payments from a bank card, Sberbank, or a YooMoney wallet.

This repository contains the SDK code and a sample app integrating it.
* [Library code](./library)
* [Code of the demo app that integrates the SDK](./sample)

# <a name="documentation"></a> Documentation

Android Checkout mobile SDK - version $versionName ([changelog](./CHANGELOG.md))

* [Changelog](#changelog)
* [Migration guide](#migration-guide)
* [Connection via Gradle](#connection-via-gradle)
* [Integration recommendations](#integration-recommendations)
    * [Recommended compatible versions](#recommended-compatible-versions)
* [Library usage](#library-usage)
    * [Tokenization](#tokenization)
        * [Launching tokenization via all methods](#launching-tokenization-via-all-methods)
        * [Launching tokenization via YooMoney wallet](#launching-tokenization-via-yoomoney-wallet)
        * [Launching tokenization via SberPay](#launching-tokenization-via-sberpay)
        * [Launching tokenization via FPS (SBP)](#launching-tokenization-via-sbp)
        * [Launching tokenization via a bank card](#launching-tokenization-via-a-bank-card)
        * [Launching tokenization for saved bank cards](#launching-tokenization-for-saved-bank-cards)
        * [Getting tokenization results](#getting-tokenization-results)
    * [Payment confirmation](#payment-confirmation)
        * [SberPay](#sberpay)
        * [3DSecure](#3dsecure)
        * [FPS (SBP)](#sbp)
    * [Linked card](#linked-card)
    * [Recurring payments](#recurring-payments)
    * [Logging and mock mode configuration](#logging-and-mock-mode-configuration)
        * [Logging enabling](#logging-enabling)
        * [Mock mode configuration](#mock-mode-configuration)
    * [Interface configuration](#interface-configuration)
    * [Scanning a bank card](#scanning-a-bank-card)
* [App moderation (permissions)](#moderation-question)
    * [Usage of location permissions](#usage-of-location-permissions)
* [Useful links](#useful-links)

# <a name="changelog"></a> Changelog

[Link to Changelog](./CHANGELOG.md)

# <a name="migration-guide"></a> Migration guide

[Link to Migration guide](./MIGRATION.md)

# <a name="connection-via-gradle"></a> Connection via Gradle

To connect the library, add the dependencies to the build.gradle of the module:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://developer.huawei.com/repo/'}
}

dependencies {
    implementation 'ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:6.10.0'
}
```

You need to add the network access settings in the application tag in your app's AndroidManifest in order for the library to work correctly.
There are two possible cases:

1) You're specifying the networkSecurityConfig attribute for the first time for your app. You need to point to the configuration file
   from the library in your AndroidManifest file in the application tag:
```
android:networkSecurityConfig="@xml/ym_network_security_config"
```

2) The networkSecurityConfig attribute is already specified in AndroidManifest.
   You need to add the following entry to this specified file:
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">certs.yoomoney.ru</domain>
</domain-config>
```

# <a name="integration-recommendations"></a> Integration recommendations

## <a name="recommended-compatible-versions"></a> Recommended compatible versions

Minimum supported Android SDK version: 24 (Android 7.0).

For the library to work correctly, you should use the following:
- Kotlin version 1.9.22 or higher
- targetSdkVersion 33
- Android Gradle plugin version 8.2.2 or higher

# <a name="library-usage"></a> Library usage


## <a name="tokenization"></a> Tokenization

The tokenization process is the exchange of payment data for a payment token (`payment_token`). This token is then used for creating a payment [via the API method](https://yookassa.ru/developers/api#create_payment)

The `Checkout.createTokenizeIntent()` method is used for starting the tokenization process. The method returns `Intent` which should be launched in `startActivityForResult()`. After that, the process will be controlled by the SDK.
The created payment token can be obtained in `onActivityResult()`

Mandatory method parameters:
- context (Context) - context of the app;
- paymentParameters (PaymentParameters) - parameters of the payment.

Optional method parameters:
- testParameters (TestParameters) - parameters for the test mode - enable logging/use mock data (see [Logging and mock mode configuration](#logging-and-mock-mode-configuration));
- uiParameters (UiParameters) - interface configuration (see [Interface configuration](#interface-configuration))

`PaymentParameters` fields:

Mandatory:
- amount (Amount) - cost of goods. Accepted payment methods may vary depending on this setting. This option will be displayed on the screen of the selected payment method in the process of tokenization via the selected payment method;
- title (String) - product name. This option will be displayed on the screen of the selected payment method in the process of tokenization via the selected payment method;
- subtitle (String) - product description. This option will be displayed on the screen of the selected payment method in the process of tokenization via the selected payment method;
- clientApplicationKey (String) - key for client apps from the YooMoney Merchant Profile ([Settings section — API Keys](https://yookassa.ru/my/api-keys-settings));
- shopId (String) - ID of the store in YooMoney ([Organization section](https://yookassa.ru/my/company/organization) - copy shopId from the required store);
- savePaymentMethod (SavePaymentMethod) - settings for saving the payment method. Saved payment methods can be used for recurring payments, (see [Recurring payments](#recurring-payments)).

Optional:
- paymentMethodTypes (Set of PaymentMethodType) - restrictions on payment methods. If you leave the field empty or specify null in it, the library will use all available payment methods, (see [Launching tokenization via all methods](#launching-tokenization-via-all-methods));
- customReturnUrl (String) - url of the page (only https is supported) that the user should be returned to after completing 3ds. **Must be used only when own Activity for the 3ds url is used**, (see [3DSecure](#3dsecure)) or, if necessary, continue working inside WebView;
- userPhoneNumber (String) - user's phone number for payments via SberPay, (see [Launching tokenization via SberPay](#launching-tokenization-via-SberPay));
- authCenterClientId (String) - unique ID of the app for tokenization via YooMoney. (See [Launching tokenization via YooMoney wallet](#launching-tokenization-via-YooMoney-wallet))

`Amount` class fields:
- value (BigDecimal) - amount;
- currency (Currency) - currency.

`SavePaymentMethod` values:
- ON - Save the payment method for recurring payments. Only payment methods that support saving will be available to the user. During the first payment, a notification stating that the payment method will be saved will appear on the screen of the selected payment method.
- OFF - Do not save the payment method.
- USER_SELECTS - The user chooses whether or not to save the payment method. If the method can be saved, a toggle will appear on the selected payment method screen.
  You can read more about recurring payments in the [Recurring payments](#recurring-payments) section.

`PaymentMethodType` values:
- YOO_MONEY - payment was made from a YooMoney wallet;
- BANK_CARD - payment was made from a bank card;
- SBERBANK - payment was made via Sberbank (text message invoicing or SberPay);
- SBP - payment was made via FPS.


### <a name="launching-tokenization-via-all-methods"></a> Launching tokenization via all methods

To launch tokenization via all available payment methods, call the `createTokenizeIntent` method with the specified `PaymentParameters` object that contains the set of all payment methods, or do not include this set.

**Option 1**:

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods,
            paymentMethodTypes = setOf(PaymentMethodType.YOO_MONEY, PaymentMethodType.BANK_CARD, PaymentMethodType.SBERBANK), // the full list of available payment methods has been provided
            gatewayId = "gatewayId", // gatewayId of the store for Google Pay payments (required if payment methods include Google Pay)
            customReturnUrl = "https://custom.redirect.url", // url of the page (only https is supported) that the user should be returned to after completing 3ds or, if necessary, continue working inside WebView;
            userPhoneNumber = "+79041234567", // user's phone number. Used to fill in the respective field in SberPay automatically. Supported data format: "+7XXXXXXXXXX"
            authCenterClientId = "example_authCenterClientId" // ID received upon registering the app on the https://yookassa.ru website
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Option 2**:

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product Description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods,
            authCenterClientId = "example_authCenterClientId", // ID received upon registering the app on the https://yookassa.ru website
            gatewayId = "gatewayId", // gatewayId of the store for Google Pay payments (required if payment methods include Google Pay)
            customReturnUrl = "https://custom.redirect.url", // url of the page (only https is supported) that the user should be returned to after completing 3ds. Must be used only when own Activity for the 3ds url is used or, if necessary, continue working inside WebView;
            userPhoneNumber = "+79041234567", // user's phone number. Used to fill in the respective field in SberPay automatically. Supported data format: "+7XXXXXXXXXX"
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="launching-tokenization-via-yoomoney-wallet"></a> Launching tokenization via YooMoney wallet

> If the payment methods include the YooMoney wallet, you will need to register the app and obtain `authCenterClientId`.
In other cases, this step can be skipped.

If you have already registered an app for **oAuth-authorization**, you can find the list of your apps at https://yookassa.ru/oauth/v2/client
If you have not yet registered an app for **oAuth-authorization**, follow the instructions below.
1. To register a new app, sign in at https://yookassa.ru
2. After signing in, navigate to the app registration page - https://yookassa.ru/oauth/v2/client
3. Click the Create app button and set the values for the parameters:
    * Name;
    * Description (optional);
    * Link to website;
    * Callback URL (any value; you can specify the link to the website);
    * Accesses. This includes three sections: `YooMoney API`, `YooMoney wallet`, `YooMoney profile`.
        * In the `YooMoney wallet` section, give permission to view the balance of the user's wallet. To do so, check the box next to the **View** field in the **WALLET BALANCE** section;
        * Open the `YooMoney profile` section and give permission to view the user's phone number, email, name, and avatar. To do so, check the box next to the **View** field in the **USER PHONE NUMBER, EMAIL, NAME, AND AVATAR** section;
3. Click or tap the "Register" button and complete the registration;
4. Information about the registered app will appear in the opened window. You will need `authCenterClientId` to launch tokenization;

<details open>
<summary>Kotlin</summary>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startYooMoneyTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product Description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods,
            paymentMethodTypes = setOf(PaymentMethodType.YOO_MONEY), // selected payment method - YooMoney wallet
            authCenterClientId = "example_authCenterClientId" // ID received upon registering the app on the https://yookassa.ru website
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="launching-tokenization-via-sberpay"></a> Launching tokenization via SberPay

Payments via SberPay allow for two user scenarios:
- Payment via the Sberbank Online mobile app;
- Payment by phone number (without the Sberbank Online mobile app);

To provide the option of paying via the Sberbank Online mobile app to your users, specify the unique scheme from your app's deep link.
The unique deep link scheme is what you specify as the scheme in your deep links. For example, in the `exampleapp://some.path` deep link, `exampleapp` is the scheme of your app.

To add a unique deep link scheme, insert the `resValue "string", "ym_app_scheme", "exampleapp"` line into the android.defaultConfig block in your build.gradle file
```
android {
    defaultConfig {
        resValue "string", "ym_app_scheme", "exampleapp"
    }
}
```
Or insert a line like this into your strings.xml:
```
<resources>
    <string name="ym_app_scheme" translatable="false">exampleapp</string>
</resources>
```
Where `exampleapp` is your app's unique deep link scheme (replace this example with your scheme).
If you are already processing some deep links in your app, you can use the ready-made scheme of your app.
If you have not processed deep links in the project before, you can create a unique scheme for your app (it must consist of Latin letters).

**Launching tokenization via SberPay looks like this.**

<details open>

```kotlin
class MyActivity: Activity() {

    private fun startSberPayTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.valueOf(10.0), Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product Description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods
            paymentMethodTypes = setOf(PaymentMethodType.SBERBANK), // selected payment method - SberPay
            userPhoneNumber = "+79041234567", // user's phone number. Used to fill in the respective field in SberPay automatically. Supported data format: "+7XXXXXXXXXX"
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="launching-tokenization-via-sbp"></a> Launching tokenization via FPS (SBP)

To add support for payments via the Faster Payments System for your users, specify the unique scheme from your app's deep link.
The unique deep link scheme is what you specify as the scheme in your deep links. For example, in the `exampleapp://some.path` deep link, `exampleapp` is the scheme of your app.

To add a unique deep link scheme, insert the `resValue "string", "ym_app_scheme", "exampleapp"` line into the android.defaultConfig block in your build.gradle file
```
android {
    defaultConfig {
        resValue "string", "ym_app_scheme", "exampleapp"
    }
}
```
Or insert a line like this into your strings.xml:
```
<resources>
    <string name="ym_app_scheme" translatable="false">exampleapp</string>
</resources>
```
Where `exampleapp` is your app's unique deep link scheme (replace this example with your scheme).
If you are already processing some deep links in your app, you can use the ready-made scheme of your app.
If you have not processed deep links in the project before, you can create a unique scheme for your app (it must consist of Latin letters).

**Launching tokenization via FPS (SBP) looks like this.**

<details open>

```kotlin
class MyActivity: Activity() {

    private fun startSBPTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.valueOf(10.0), Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product Description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods
            paymentMethodTypes = setOf(PaymentMethodType.SBP), // selected payment method - SBP
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="launching-tokenization-via-a-bank-card"></a> Launching tokenization via a bank card

To launch tokenization via just a bank card, specify `setOf(PaymentMethodType.BANK_CARD)` in `PaymentParameters`:

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startBankCardTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods,
            paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD) // selected payment method - bank card
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="launching-tokenization-for-saved-bank-cards"></a> Launching tokenization for saved bank cards

This tokenization method is used when a bank card is linked to the store (see [Linked card](#linked-card)) and you need to re-request its CSC from the user.
In other cases, the standard tokenization mechanism should be used (see [Launching tokenization](#tokenization)).

To start the tokenization process with a payment ID, use the `Checkout.createSavedCardTokenizeIntent()` method. The method returns an `Intent` which should be launched in `startActivityForResult()`.
The created payment token can be obtained in `onActivityResult()` (see [Getting tokenization results](#getting-tokenization-results))

Mandatory method parameters:
- context (Context) - context of the app;
- savedBankCardPaymentParameters (SavedBankCardPaymentParameters) - parameters of the payment via a saved bank card.

Optional method parameters:
- testParameters (TestParameters) - parameters for the test mode - enable logging/use mock data (see [Logging and mock mode configuration](#logging-and-mock-mode-configuration));
- uiParameters (UiParameters) - interface configuration (see [Interface configuration](#interface-configuration)).

`SavedBankCardPaymentParameters` fields:

Mandatory:
- amount (Amount) - cost of goods. Accepted payment methods may vary depending on this setting;
- title (String) - product name;
- subtitle (String) - product description;
- clientApplicationKey (String) - key for client apps from the YooMoney Merchant Profile ([Settings section — API Keys](https://yookassa.ru/my/api-keys-settings));
- shopId (String) - ID of the store in YooMoney ([Organization section](https://yookassa.ru/my/company/organization) - copy shopId from the required store);
- paymentMethodId (String) - payment ID;
- savePaymentMethod (SavePaymentMethod) - settings for saving the payment method. Saved payment methods can be used for recurring payments, (see [Recurring payments](#recurring-payments)).

Optional:
- gatewayId (String) - gatewayId for the store, Google Pay).

`Amount` class fields:
* value (BigDecimal) - amount;
* currency (Currency) - currency.

`SavePaymentMethod` values:
* ON - Save the payment method for recurring payments. Only payment methods that support saving will be available to the user. During the first payment, a notification stating that the payment method will be saved will appear on the screen of the selected payment method.
* OFF - Do not save the payment method.
- USER_SELECTS - The user chooses whether to save the payment method or not. If the method can be saved, a toggle will appear on the selected payment method screen.
  You can read more about recurring payments in the [Recurring payments](#recurring-payments) section.

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startSavedCardTokenize() {
        val parameters = SavedBankCardPaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile
            shopId = "12345", // ID of the store in the YooMoney system
            paymentMethodId = "paymentMethodIdExample", // unique payment ID
            savePaymentMethod = SavePaymentMethod.OFF
        )
        val intent = createSavedCardTokenizeIntent(this, parameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

**Processed results of the tokenization process can be found in** [Get tokenization results](#get-tokenization-results) **section**


### <a name="getting-tokenization-results"></a> Getting tokenization results

The results of the `Checkout.createTokenizeIntent` method can be processed in the `onActivityResult()` method.

Possible types of results:
* Activity.RESULT_OK - tokenization was successful;
* Activity.RESULT_CANCELED - tokenization canceled by user;

In case of successful tokenization, mSDK will return the `TokenizationResult` entity that contains the token and the type of payment tool used to receive it.
Use the `Checkout.createTokenizationResult()` method to get the token.

`TokenizationResult` stores:
* paymentToken (String) - payment token;
* paymentMethodType (PaymentMethodType) - payment method type.

`PaymentMethodType` values:
* YOO_MONEY - payment was made from a YooMoney wallet;
* BANK_CARD - payment was made from a bank card;
* SBERBANK - payment was made via Sberbank (text message invoicing or SberPay);

<details open>

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TOKENIZE) {
            when (resultCode) {
                RESULT_OK -> {
                    // successful tokenization
                    val result = data?.let { createTokenizationResult(it) }
                }
                RESULT_CANCELED -> {
                    // user canceled tokenization
                }
            }
        }
    }
}
```
</details>

## <a name="payment-confirmation"></a> Payment confirmation

If necessary, the system may request a payment confirmation process, where the user confirms the transaction via third-party services (you received confirmation_url in response during the payment).
There are two types of payment confirmation: 3Dsecure (for payments via bank card) and push notifications or App2App script (for payments via SberPay or FPS).

### <a name="sberpay"></a> SberPay

To confirm a payment made via SberPay, you must:
1. call the Checkout.createConfirmationIntent() method;
2. process the received result in the onActivityResult method in Activity;

Input parameters for `Checkout.createConfirmationIntent()`:

Mandatory method parameters:
- context (Context) - context of the app;
- confirmationUrl (String) - URL for proceeding to 3DS;
- paymentMethodType (PaymentMethodType) - selected type of payment method (the one that was received in the `createTokenizationResult()` method, (see [Getting tokenization results](#getting-tokenization-results)).
- clientApplicationKey (String) - key for client apps from the YooMoney Merchant Profile ([Settings section — API Keys](https://yookassa.ru/my/api-keys-settings))
- shopId (String) - ID of the store in YooMoney ([Organization section](https://yookassa.ru/my/company/organization) - copy shopId from the required store)

Optional method parameters:
- testParameters (TestParameters) - parameters for the test mode - enable logging/use mock data (see [Logging and mock mode configuration](#logging-and-mock-mode-configuration));
- colorScheme (ColorScheme) - color scheme, (see [Interface configuration](#interface-configuration)).

Possible types of results of 3DSecure initiation:

- Activity.RESULT_OK - indicates that the 3ds process is complete, but does not guarantee that it was successful. After receiving the result, we recommended requesting the payment status;
- Activity.RESULT_CANCELED - 3ds authentication was canceled (for example, the user tapped the "back" button during the process);
- Checkout.RESULT_ERROR -  3ds authentication failed.

**Launching 3ds and getting the results**

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startConfirmSberPay() {
        val intent = createConfirmationIntent(
            this, 
            "https://confimation-url/", 
            PaymentMethodType.SBERBANK,
            "clientApplicationKey",
            "shopId"
        )
        startActivityForResult(intent, REQUEST_CODE_CONFIRM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CONFIRM) {
            when (resultCode) {
                RESULT_OK -> return // 3ds process completed, no guarantee of success
                RESULT_CANCELED -> return // The 3ds screen was closed
            }
        }
    }
}
```
</details>

### <a name="3dsecure"></a> 3DSecure

To conduct 3DSecure for a payment made via a bank card, you can use Activity provided by the mSdk library:
1. during the tokenization process, don't specify the `customReturnUrl` parameter in the `createTokenizeIntent` method (see [Launching tokenization via a bank card](#launching-tokenization-via-a-bank-card));
2. call the Checkout.createConfirmationIntent() method;
3. process the received result in the onActivityResult method in Activity;

Input parameters for `Checkout.createConfirmationIntent()`:

Mandatory method parameters:
- context (Context) - context of the app;
- confirmationUrl (String) - URL for proceeding to 3DS;
- paymentMethodType (PaymentMethodType) - selected type of payment method (the one that was received in the `createTokenizationResult()` method, (see [Getting tokenization results](#getting-tokenization-results)).
- clientApplicationKey (String) - key for client apps from the YooMoney Merchant Profile ([Settings section — API Keys](https://yookassa.ru/my/api-keys-settings));
- shopId (String) - ID of the store in YooMoney ([Organization section](https://yookassa.ru/my/company/organization) - copy shopId from the required store)

Optional method parameters:
- testParameters (TestParameters) - parameters for the test mode - enable logging/use mock data (see [Logging and mock mode configuration](#logging-and-mock-mode-configuration));
- colorScheme (ColorScheme) - color scheme, (see [Interface configuration](#interface-configuration)).

Possible types of results of 3DSecure initiation:

- Activity.RESULT_OK - indicates that the 3ds process is complete, but does not guarantee that it was successful. After receiving the result, we recommended requesting the payment status;
- Activity.RESULT_CANCELED - 3ds authentication was canceled (for example, the user tapped the "back" button during the process);
- Checkout.RESULT_ERROR -  3ds authentication failed.

**Launching 3ds and getting the results**

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun start3DSecure() {
        val intent = createConfirmationIntent(
            this, 
            "https://3dsurl.com/", 
            PaymentMethodType.BANK_CARD,
            "clientApplicationKey",
            "shopId"
        )
        startActivityForResult(intent, REQUEST_CODE_CONFIRM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CONFIRM) {
            when (resultCode) {
                RESULT_OK -> return // 3ds process completed, no guarantee of success
                RESULT_CANCELED -> return // The 3ds screen was closed
                Checkout.RESULT_ERROR -> {
                    // An error occurred during 3ds (no connection or another reason)
                    // More information can be found in data
                    // data.getIntExtra(Checkout.EXTRA_ERROR_CODE) - error code from WebViewClient.ERROR_* or Checkout.ERROR_NOT_HTTPS_URL
                    // data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION) - error description (may be missing)
                    // data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL) - url where the error occurred (may be missing)
                }
            }
        }
    }
}
```
</details>

### <a name="sbp"></a> FPS (SBP)

To confirm a payment made via FPS (SBP), you must:
1. call the Checkout.createConfirmationIntent() method;
2. process the received result in the onActivityResult method in Activity;

Input parameters for `Checkout.createConfirmationIntent()`:

Mandatory method parameters:
- context (Context) - context of the app;
- confirmationUrl (String) - deeplink for the redirect to FPS (SBP);
- paymentMethodType (PaymentMethodType) - selected type of payment method (the one that was received in the `createTokenizationResult()` method, (see [Getting tokenization results](#getting-tokenization-results)).
- clientApplicationKey (String) - key for client apps from the YooMoney Merchant Profile ([Settings section — API Keys](https://yookassa.ru/my/api-keys-settings));
- shopId (String) - ID of the store in YooMoney ([Organization section](https://yookassa.ru/my/company/organization) - copy shopId from the required store)

Optional method parameters:
- testParameters (TestParameters) - parameters for the test mode - enable logging/use mock data (see [Logging and mock mode configuration](#logging-and-mock-mode-configuration));
- colorScheme (ColorScheme) - color scheme, (see [Interface configuration](#interface-configuration)).

Possible types of results of 3DSecure initiation:

- Activity.RESULT_OK - indicates that the FPS (SBP) process is completed, but does not guarantee that it was successful. We recommend requesting the payment status after receiving the result;
- Activity.RESULT_CANCELED - FPS (SBP) authentication was canceled (for example, the user tapped the "back" button during the process);
- Checkout.RESULT_ERROR -  FPS (SBP) authentication failed.

**Launching FPS (SBP) and getting the results**

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startConfirmSBP() {
        val intent = createConfirmationIntent(
            this, 
            "https://sbp_confirmation.com/", 
            PaymentMethodType.SBP,
            clientApplicationKey = "live_thisKeyIsNotReal",
            shopId = "your_shopId"
        )
        startActivityForResult(intent, REQUEST_CODE_CONFIRM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CONFIRM) {
            when (resultCode) {
                RESULT_OK -> return // SBP process completed, no guarantee of success
                RESULT_CANCELED -> return // The SBP screen was closed
            }
        }
    }
}
```
</details>

## <a name="linked-card"></a> Linked card

Specify the optional parameter `customerId` in the method for tokenization (see [Launching tokenization via a bank card](#launching-tokenization-via-a-bank-card)) so that after the tokenization with a bank card, this bank card would be successfully linked and displayed in the list of payment methods available to the user. The card is linked only when the user consents to its linking.
- customerId (String) - unique ID of the customer in your system, such as email addres or phone number. Limit by length: no more than 200 characters.
  **Make sure the customerId corresponds to the user who wants to make a purchase. For example, use two-factor authentication. If you specify an incorrect ID, the user will be able to select other people's bank cards for payment.**

Therefore, the method for carrying out the tokenization of a bank card with the subsequent linking of this bank card is as follows:

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startLinkedBankCardTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.OFF, // flag of the disabled option to save payment methods,
            paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD), // selected payment method - bank card,
            customerId = "uniqueCustomerId" // unique ID of the customer in your system
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

You can use the test mode to see how the linked card will look on the screen with the list of payment methods.
You must set the number of cards in the `linkedCardsCount` parameter of the `MockConfiguration` object and call the `createTokenizeIntent` method. Learn more in the [Logging and mock mode configuration](#logging-and-mock-mode-configuration) section

## <a name="recurring-payments"></a> Recurring payments

Recurring payments are payments that are repeated in accordance with a specific schedule. Debits for such payments are made automatically after the consent of the user.

You can save some payment methods and use them for recurring payments. In this case, the user needs to confirm only the first payment, and subsequent debits will be unconditional. [You can learn more here](https://yookassa.ru/developers/payments/recurring-payments)

To enable autopayments, specify one of the following values to the `savePaymentMethod` parameter during the [tokenization](#tokenization) process:
- `SavePaymentMethod.ON` - save the payment method for recurring payments. Only payment methods that support saving will be available to the user. During the first payment, a notification stating that the payment method will be saved will appear on the screen of the selected payment method.<br/>
  ![SavePaymentMethod.ON](assets/images/recurrent-payments/save-payment-method-on.png)
- `SavePaymentMethod.USER_SELECTS` - the user chooses whether or not to save the payment method. If the method can be saved, a toggle will appear on the selected payment method screen.<br/>
  ![SavePaymentMethod.USER_SELECTS](assets/images/recurrent-payments/save-payment-method-user-selects.png)

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startTokenize() {
        val paymentParameters = PaymentParameters(
            amount = Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
            title = "Product name",
            subtitle = "Product description",
            clientApplicationKey = "live_thisKeyIsNotReal", // key for client apps from the YooMoney Merchant Profile (https://yookassa.ru/my/api-keys-settings)
            shopId = "12345", // ID of the store in the YooMoney system
            savePaymentMethod = SavePaymentMethod.ON, // flag of the disabled option to save payment methods
            authCenterClientId = "example_authCenterClientId", // ID received upon registering the app on the https://yookassa.ru website
            gatewayId = "gatewayId", // gatewayId of the store for Google Pay payments (required if payment methods include Google Pay)
            customReturnUrl = "https://custom.redirect.url", // url of the page (only https is supported) that the user should be returned to after completing 3ds. Must be used only when own Activity for the 3ds url is used or, if necessary, continue working inside WebView;
            userPhoneNumber = "+79041234567", // user's phone number. Used to fill in the respective field in SberPay automatically. Supported data format: "+7XXXXXXXXXX"
        )
        val intent = createTokenizeIntent(this, paymentParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

> After receiving the token for a recurring payment, [make the payment](https://yookassa.ru/developers/payment-acceptance/integration-scenarios/mobile-sdks/payments-with-tokens) and after making a payment you will receive a payment token (do not confuse it with the token received in msdk) and you will be able to make a [recurring payment](https://yookassa.ru/developers/payment-acceptance/scenario-extensions/recurring-payments#pay-with-saved) using it in the future.

## <a name="logging-and-mock-mode-configuration"></a> Logging and mock mode configuration

#### <a name="logging-enabling"></a> Logging enabling

You may need to include logs in the sdk for the purposes of debugging. To do that, specify the `showLogs = true` parameter in `TestParameters` when launching tokenization

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startTokenize() {
        val testParameters = TestParameters(
            showLogs = true // showLogs - enable/disable display of sdk logs
        )
        val intent = createTokenizeIntent(this, paymentParameters, testParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

> Please note that the logs will only be displayed in the debug app. Do not specify this flag for the release build of the app.

#### <a name="mock-mode-configuration"></a> Mock mode configuration

The library has a mock mode which you can use to see what the SDK would look like with various inputs. This mode does not require Internet access. The received token can't be used for payment.

`MockConfiguration` class fields:
- `completeWithError` (Boolean) - tokenization always returns an error;
- `paymentAuthPassed` (Boolean) - whether the user is authorized to pay with a wallet or not;
- `linkedCardsCount` (Int) - number of cards linked to the user's wallet;
- `serviceFee` (Amount) - commission that will be displayed on the screen of the selected payment method;

To run tokenization in mock mode, specify the `mockConfiguration` parameter in `TestParameters`

<details open>

```kotlin
class MyActivity : AppCompatActivity() {

    fun startTokenize() {
        val testParameters = TestParameters(
            showLogs = true, // showLogs - enable/disable display of SDK logs
            googlePayTestEnvironment = true, // googlePayTestEnvironment - what type of environment should be used for Google Pay, test or production. Learn more at https://developers.google.com/pay/api/android/guides/test-and-deploy/integration-checklist
            mockConfiguration = MockConfiguration(
                completeWithError = true, // completeWithError - always return an error during tokenization
                paymentAuthPassed = true, // paymentAuthPassed - whether the user is authorized to pay with a wallet or not
                linkedCardsCount = 3, // linkedCardsCount - number of cards linked to the user's wallet;
                serviceFee = Amount(BigDecimal.ONE, Currency.getInstance("RUB")) // serviceFee - commission that will be displayed on the screen of the selected payment method
            )
        )
        val intent = createTokenizeIntent(this, paymentParameters, testParameters)
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
}
```
</details>

## <a name="interface-configuration"></a> Interface configuration

#### <a name="UiParameters-ColorScheme"></a> UiParameters and ColorScheme

To customize the SDK interface, you can use the `UiParameters` object, namely set the main color of the interface and configure the display of the YooMoney logo on the screen with the list of payment methods (hide or show).

`UiParameters` class fields:
* showLogo (Boolean) - show or hide the YooMoney logo on the screen with the list of payment methods;
* colorScheme (ColorScheme) - mSdk color scheme in your app.

`ColorScheme` class fields:
* primaryColor (ColorInt) - the main color of the app. Buttons, switches, input fields, etc. will be switched to this color.
  We don't recommend making this color too light if your theme is already designed with light colors, as it won't be visible on a white background, or choose a dark color for a dark theme, since it won't be visible either. We also don't recommend using red as it might mix up with color of error messages.

  | With icon     | Without icon       |
          | ------------- |:------------------:|
  | ![List of payments with icon](assets/images/custom_ui_parameters_examples/1_show_logo_true.png)     | ![List of payments without icon](assets/images/custom_ui_parameters_examples/2_show_logo_false.png)   |

  | Correct custom color     | Incorrect custom color         |
          | ------------- |:------------------:|
  | ![Correct color](assets/images/custom_ui_parameters_examples/3_good_color_example.png)     | ![Incorrect color](assets/images/custom_ui_parameters_examples/4_bad_color_example.png)   |

  <details open>

  ```kotlin
  class MyActivity : AppCompatActivity() {

      fun tokenizeCustomUiParameters() {
          val paymentParameters = PaymentParameters(...)
          val uiParameters = UiParameters(
              showLogo = true,
              colorScheme = ColorScheme(Color.rgb(0, 114, 245))
          ) // specify the true flag so that the YooMoney logo is displayed on the screen with the list of payment methods and specify the ColorScheme that contains the custom color
          val intent = createTokenizeIntent(
              context = this,
              paymentParameters = paymentParameters,
              uiParameters = uiParameters
          )
          startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
      }
  }
  ```
  </details>

#### <a name="disabling-the-dark-theme"></a> Disabling the dark theme

Quite often, on Xiaomi devices, you can encounter the problem of color inversion when the dark theme is enabled on the device. The standard solution to this problem is to disable the dark theme at the system level. To do this, follow the next steps:

1. Override the ym_Theme by adding the following code to the styles of your application:

<details open>

```xml
<style name="ym_Theme.Translucent" parent="Theme.Yoo">
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:colorBackgroundCacheHint">@null</item>
    <item name="android:windowTranslucentStatus" tools:targetApi="kitkat">true</item>
    <item name="android:windowTranslucentNavigation" tools:targetApi="kitkat">true</item>
    <item name="android:windowIsTranslucent">@bool/ym_windowIsTranslucent</item>
    <item name="android:windowAnimationStyle">@android:style/Animation</item>
    <item name="android:windowIsFloating">@bool/ym_windowIsFloating</item>
    <item name="android:backgroundDimEnabled">false</item>
    <item name="colorAccent">@color/ym_palette_accent</item>
    <item name="ym_ListItemSwitchCheckout_Style">@style/ym_ListItemSwitch</item>
    <item name="android:forceDarkAllowed" tools:targetApi="q">false</item>
</style>

<style name="ym_Theme" parent="Theme.Yoo">
    <item name="android:windowLightStatusBar" tools:ignore="NewApi">true</item>
    <item name="colorAccent">@color/ym_palette_accent</item>
    <item name="colorControlNormal">#1e000000</item>
    <item name="android:alertDialogTheme">@style/ym_DialogStyleColored</item>
    <item name="ym_ListItemSwitchCheckout_Style">@style/ym_ListItemSwitch</item>
    <item name="android:forceDarkAllowed" tools:targetApi="q">false</item>
</style>
```
</details>

2. Disable the dark theme by adding a call to the `setDefaultNightMode()` function with the `AppCompatDelegate.MODE_NIGHT_NO` parameter to your implementation of the Application class:

<details open>

```kotlin
AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
```
</details>

## <a name="scanning-a-bank-card"></a> Scanning a bank card

The sdk includes the option to connect a library for scanning bank cards with a camera. If you enable scanning, a special button will appear on the bank card number entry form ![Card scanning icon](assets/images/scan-bank-card/icon-scanner.png):

![Bank card details entry screen](assets/images/scan-bank-card/bank-card-form-with-scan.png)

When the user taps this button, the sdk will launch `intent` to launch `activity` that implements card scanning.
>Bank card scanning is not implemented in the sdk itself, the scanning is proposed to be implemented in `activity` via a third-party library instead.

To do that, create `activity` that will handle the `ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD` action, then register this `activity` in `AndroidManifest.xml`, specifying `intent-filter` for the  `ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD` action:
```xml
<activity android:name=".ScanBankCardActivity">

    <intent-filter>
        <action android:name="ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD"/>
    </intent-filter>

</activity>
```

Now when you tap the card scanning button ![Card scanning icon](assets/images/scan-bank-card/icon-scanner.png), your `activity` will be launched where you will need to implement the card scanning, then complete `activity` and specify the resuls using the `setResult` method.

To do that, before completing `activity`, call the `Checkout.createScanBankCardResult` method and specify the card number and optional month/year parameters, and send it to the `setResult` method. Then call the `finish()` method

<details open>

```kotlin
class ScanBankCardActivity : Activity() {

    private fun onScanningDone(cardNumber: String, expirationMonth: Int, expirationYear: Int) {
        val result: Intent = Checkout.createScanBankCardResult(cardNumber, expirationMonth, expirationYear)
        setResult(RESULT_OK, result)
        finish()
    }
}
```
</details>

#### <a name="example-of-`activity`-implementation-for-scanning-bank-card-details"></a> Example of `activity` implementation for scanning bank card details
Below you can find the example of bank card data scanning.

<details open>

```kotlin
class ScanBankCardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this,
                    "Give permisson to use the camera in the settings",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                startScanProcess()
            }
        }
    }

    private fun startScanProcess() {
        val cardNumber = "2202 2695 2695 2695"
        val month = 12
        val year = 30

        Toast.makeText(
            this,
            "Add your own scanner implementation",
            Toast.LENGTH_SHORT
        ).show()

        val isCardDataValid: Boolean = isCardsDataValid(cardNumber, month, year)
        if (isCardDataValid) {
            //send scanning result to checkout yoomoney
            val scanBankCardResult: Intent = Checkout.createScanBankCardIntent(
                cardNumber,
                month,
                year
            )
            setResult(Activity.RESULT_OK, scanBankCardResult)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        
        finish()
    }

    private fun isCardsDataValid(
        cardNumber: String,
        month: Int,
        year: Int,
    ): Boolean = true

    companion object {
        const val REQUEST_CODE = 1
    }
}
```
</details>

# <a name="moderation-question"></a> Permission usage

## <a name="usage-of-location-permissions"></a> Usage of location permissions

The YooMoney for Business Payments SDK uses the AppMetrica library. Since Appmetrica has access to the location, Google may in some cases ask to update the privacy policy by mentioning that user data may be transferred to third-party companies.

# <a name="useful-links"></a> Useful links
* [YooMoney website](https://yookassa.ru)
* [Mobile SDK documentation on the YooMoney website](https://yookassa.ru/docs/client-sdks/#mobil-nye-sdk)
* [RuStore demo app](https://apps.rustore.ru/app/ru.yoo.sdk.kassa.payments.example.release)
* [SDKs for iOS](https://git.yoomoney.ru/projects/SDK/repos/yookassa-payments-swift)