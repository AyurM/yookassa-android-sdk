# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# SberPay Sdk
-keep class spay.sdk.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# OkHttp ------------------------------------------------------------------------------------------
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# BI.ZONE Fingerprint ------------------------------------------------------------------------------------------
# keep Serializable classes
-keepclassmembers class zone.bi.mobile.fingerprint.** implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#java lambda
-dontwarn java.lang.invoke.**

# Google Play Services ------------------------------------------------------------------------------------------
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# BI.ZONE Fingerprint Native ------------------------------------------------------------------------------------------
-keep class zone.bi.mobile.fingerprint.impl.cs.FpWorkerService { <init>(...);}
-keeppackagenames zone.bi.mobile.**
-keeppackagenames bz.**
-keeppackagenames npi.spay.**

-keepclasseswithmembers,allowshrinking,allowoptimization class zone.bi.mobile.fingerprint.impl.ntl.Brg {
    native <methods>;
}

# profiling SDK wants OkHttp to be available like this
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class org.threeten.bp.** { *; }
-keep class ru.yoomoney.sdk.auth.model.** { *; }
-keep class ru.yoomoney.sdk.auth.ProcessType** { *; }
-keep class ru.yoomoney.sdk.kassa.payments.Checkout { *; }
-keep class ru.yoomoney.sdk.kassa.payments.ui.** { *; }
-keep class androidx.lifecycle.YooKassaKeyedFactory { *; }
-keep class androidx.lifecycle.ViewModelKeyedFactory { *; }
-keep class androidx.lifecycle.YooKassaViewModelProvider { *; }
-keep class ru.yoomoney.sdk.kassa.payments.utils.WebTrustManagerImpl { *; }
-keep class ru.yoomoney.sdk.kassa.payments.utils.WebTrustManager { *; }

-keeppackagenames ru.yoomoney.sdk.kassa.payments.**
-keeppackagenames ru.yoomoney.sdk.kassa.payments
-keeppackagenames com.group_ib.sdk

-keep class ru.yoomoney.sdk.yooprofiler.* { *; }

-dontwarn javax.annotation.Nullable
-dontwarn org.conscrypt.OpenSSLProvider
-dontwarn org.conscrypt.Conscrypt
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn java.lang.invoke.StringConcatFactory

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Keep API models for Jackson
-keep class ru.yoomoney.sdk.kassa.payments.api.model.** { *; }
-keep class ru.yoomoney.sdk.kassa.payments.model.Config { *; }
-keep class ru.yoomoney.sdk.kassa.payments.model.ConfigPaymentOption { *; }
-keep class ru.yoomoney.sdk.kassa.payments.model.SavePaymentMethodOptionTexts { *; }
-keep class retrofit2.converter.jackson.ResultJacksonResponseBodyConverter { *; }
-keep class ru.yoomoney.sdk.kassa.payments.api.YooKassaJacksonConverterFactory { *; }
-keep class ru.yoomoney.sdk.kassa.payments.api.JacksonBaseObjectMapperKt { *; }
-keep class ru.yoomoney.sdk.kassa.payments.utils.compose.BottomSheetCornersKt { *; }
-keep class ru.yoomoney.sdk.kassa.payments.utils.compose.OnLifecycleEventKt { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class ru.yoomoney.sdk.auth.account.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class kotlin.Result
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation