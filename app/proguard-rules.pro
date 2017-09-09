# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\matejd\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

### DO NOT OBFUSCATE (helps with bug reports)

-dontobfuscate
-optimizations !code/allocation/variable

### RETROFIT

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn rx.**
-dontwarn retrofit.appengine.UrlFetchClient

### PICASSO

-dontwarn com.squareup.okhttp.**

### ARCHITECTURE COMPONENTS

-keep class android.arch.** { *; }

-keep class com.matejdro.taskerspotifystarter.config.StartPlaybackSetupActivity { <init>(...); }