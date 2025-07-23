> #### Before create pull request
> - You must specify one of the versions in the field **NEXT_VERSION_TYPE**
> - Also you need to indicate descriptions of changes between fields **NEXT_VERSION_DESCRIPTION_BEGIN** and **NEXT_VERSION_DESCRIPTION_END**
### NEXT_VERSION_TYPE=MAJOR|MINOR|PATCH
### NEXT_VERSION_DESCRIPTION_BEGIN
### NEXT_VERSION_DESCRIPTION_END

## [7.1.1] (06-06-2025)

- update appmetrica (5.0.0 -> 7.9.0)

## [7.1.0] (26-03-2025)

- update spaysdk (2.4.1 -> 2.5.1)
- change SBP endpoint and business logic

## [7.0.3] (05-02-2025)

- update spaysdk (2.3.0 -> 2.4.1)
- fix wallet contract with empty paymentMethodTypes
- fix contract loading

## [7.0.2] (19-12-2024)

- fix certs host bug
- update pinning (1.5.0 -> 1.6.0)

## [7.0.1] (11-10-2024)

update spaysdk (2.2.4 -> 2.3.0)

## [7.0.0] (04-09-2024)

- update dependencies: AGP 8.3.2, pinning 1.5.0, auth 2.10.0, profiler 3.3.0
- change distributionUrl to public
- change cards scanner example
- add sberPay

## [6.11.0] (24-04-2024)

- update dependencies: java 17, kotlin 1.9.22, AGP 8.2.2, fix proguard collision problem, fix problem with SBP to handle situation with returning back from bank app to MSDK

## [6.10.1] (15-02-2024)

- Update README.md files

## [6.10.0] (15-02-2024)

- Compose integration, update min android sdk (21 -> 24)

## [6.9.4] (19-01-2024)

- fix problems with DNKA mode

## [6.9.3] (10-01-2024)

- Fix problems with injection

## [6.9.2] (13-11-2023)

- Support fullMode obfuscation mode

## [6.9.1] (10-11-2023)

* Updates for autotests

## [6.9.0] (03-11-2023)

- The design has been updated, changes have been made in SBP payment option

## [6.8.0] (22-08-2023)

- add sbp bank search, update kotlin, other dependencies

## [6.7.1] (31-07-2023)

- Fixed mistakes in documentation

## [6.7.0] (20-07-2023)

- Added new payment option - SBP

## [6.6.4] (04-07-2023)

- Add artefacts size param for grafana metrics to Buildfile

## [6.6.3] (26-06-2023)

- Update UI and Auth libraries

## [6.6.2] (05-06-2023)

- Fix crash at the start of the confirmation process

## [6.6.1] (12-05-2023)

- Fix crash at the start of the library on Android 8

## [6.6.0] (27-04-2023)

- add payment agent information

## [6.5.8] (05-04-2023)

- Update readme documentation

## [6.5.7] (01-03-2023)

- update readme

## [6.5.6] (09-02-2023)

- Add new Profiling library

## [6.5.5] (19-01-2023)

- Remove TMX Profiling library

## [6.5.4] (08-12-2022)

- add sonar-project.properties

## [6.5.3] (04-07-2022)

- Upgrade AppMetrica version

## [6.5.2] (05-04-2022)

- Solved the problem with YooMoney tokenization

## [6.5.1] (23-03-2022)

- Solved the problem with launching sample application

## [6.5.0] (16-03-2022)

- Hide ThreatMetrix into mSdk
- Update documentation

## [6.4.5] (06-12-2021)

- Add confirmation in tokenize

## [6.4.4] (11-11-2021)

- Update auth lib

## [6.4.3] (09-11-2021)

- Rename file META-INF/ui_release.kotlin_module

## [6.4.2] (20-10-2021)

- Rename file META-INF/core_release.kotlin_module

## [6.4.1] (12-10-2021)

- Fixed proguard rules for packages
- Updated auth-sdk library
- Replaced <fragment> FragmentContainerView

## [6.4.0] (29-09-2021)

- Added getting static data in the config
- Added recurring payments for SberPay

## [6.3.0] (16-09-2021)

- Addded support of linked to merchant shop cards and safe payment

## [6.1.5] (30-08-2021)

- Added english version of readme

## [6.1.4] (03-08-2021)

- Updated auth-sdk version and fixed crash with authentication in YooMoney

## [6.1.3] (12-07-2021)

- Updated Google Pay icon

## [6.1.2] (09-07-2021)

- Added support of recurrent payments for gpay;
- Updated gradle version;

### 6.0.0
2021-05-28
- Added authentication with YooMoney app
- Added payments with Sberpay app

### 6.0.0
2021-05-28
- Added authentication with YooMoney app
- Added payments with Sberpay app

### 5.1.0
2021-03-09
- Updated library architecture, improved stability
- Updated the library's user interface

### 5.0.3
2020-12-15
- Updated gpay gateway

### 5.0.2
2020-12-14
- Updated auth-sdk, changed 3ds urls

### 5.0.1
2020-11-30
- Renamed packages

### 4.1.0
2020-11-6
- Updated auth-sdk version

### 4.0.0
2020-10-20
- Replace appcompat libraries with androidx
- Replace YandexAuthSdk with YooMoney auth-sdk

### 3.0.4
2020-07-24
- Bug fixes and improvements
- Fix scanning of Bank cards on some devices in demo app

### 3.0.3
2020-04-8
- Temporary measure: accepting expired cards (card expiry date must not be earlier than 2020-01-01)
- Update README.
- Update CODEOWNERS.

### 3.0.2
2020-01-23
- Fix minor crashes.

### 3.0.1
2019-12-20
- Update translations.

### 3.0.0
2019-12-18
- Change public api. PaymentParameters and SavedBankCardPaymentParameters now have new required parameter - savePaymentMethod.
- Add ability to save payment methods for recurring payments.

### 2.4.1
2019-11-15
- Fix screen artifacts on closing mSDK.

### 2.4.0
2019-09-06
- Add bank icons for card screen.
- Add tokenization flow for saved bank cards.

### 2.3.0
2019-08-08
- Add info about the fee on the contract screen.
- Fix 3-DS screen closing after Google Pay tokenization.
- Fix keyboard behaviour on user logout.
- Fix scrolling on bank card screen.
- Fix rare crashes after saving instance state.

### 2.2.1
2019-05-16
- ThreatMetrix fix.

### 2.2.0
2019-04-25
- Add new parameter "userPhoneNumber" in PaymentParameters.
- Add setting of supported card networks in Google Pay.
- Fix crash on 3-DS screen (https://github.com/yoomoney/yookassa-android-sdk/issues/26).
- Update README.

### 2.1.0
2019-04-01
- Add color customization.
- Add support for AndroidX (https://github.com/yoomoney/yookassa-android-sdk/issues/21).
- Fix Google Pay disabling from AndroidManifest (https://github.com/yoomoney/yookassa-android-sdk/issues/20).

### 2.0.0
2019-01-25
- Change public api. Refactor Checkout methods and public classes.
- Add javadoc for public classes.
- Add logging. Logging can be turned on in TestParameters object.
- Change 3ds process. Built-in 3ds screen now uses an internal redirect url. If you want to use your own 3ds screen, you can specify customReturnUrl in PaymentParameters object.
- Update README.

### 1.3.0.7
2019-01-24
- Add PAN_ONLY cards from Google Pay as payment option.

### 1.3.0.6
2018-12-27
- Fix 3ds for test payments (https://github.com/yoomoney/yookassa-android-sdk/issues/9).

### 1.3.0.5
2018-12-18
- Fix memory leak.
- Fix SSL handshake problem on Android 4.

### 1.3.0.4
2018-11-23
- ThreatMetrix fix.

### 1.3.0.3
2018-08-15
- Fix crash on bank card screen when user clicks "done" without filling in a card number (https://github.com/yoomoney/yookassa-android-sdk/issues/5).
- Fix keyboard opening automatically after MSDK dismiss (https://github.com/yoomoney/yookassa-android-sdk/issues/3).

### 1.3.0.2
2018-08-02
- Fix 3ds screen crash (https://github.com/yoomoney/yookassa-android-sdk/issues/4).

### 1.3.0.1
2018-07-17
- Fix typos in README.
- Change MSDK behavior when only one payment option is available.

### 1.3.0
Initial public version.