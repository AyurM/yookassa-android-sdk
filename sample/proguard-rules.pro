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
-printconfiguration ../all-proguard-rules.txt

#remove rules after updating msdk > 6.10.1
# Java warnings
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
# VK warnings
-dontwarn com.vk.auth.**
-dontwarn com.vk.location.**
-dontwarn com.vk.oauth.**
-dontwarn com.vk.stat.**
-dontwarn com.vk.superapp.**
-dontwarn com.vk.silentauth.**
-dontwarn com.vk.api.sdk.VK
-dontwarn org.slf4j.impl.StaticLoggerBinder