# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\androidsdk\AndroidSdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-------------------------------代码混淆--------------------------
    #-----------------混淆配置设定------------------------------------------------------------------------
    -optimizationpasses 5                                                       #指定代码压缩级别
    -dontusemixedcaseclassnames                                                 #混淆时不会产生形形色色的类名
    -dontskipnonpubliclibraryclasses                                            #指定不忽略非公共类库
    -dontpreverify                                                              #不预校验，如果需要预校验，是-dontoptimize
    -ignorewarnings                                                             #屏蔽警告
    -verbose                                                                    #混淆时记录日志
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    #优化

    #1、
    #-----------------不需要混淆第三方类库------------------------------------------------------------------
    -dontwarn android.support.v4.**                                             #去掉警告
    -keep class android.support.v4.** { *; }                                    #过滤android.support.v4
    -keep interface android.support.v4.app.** { *; }
    -keep public class * extends android.support.v4.**
    -keep public class * extends android.app.Fragment

    #2、
    #-----------------不需要混淆系统组件等-------------------------------------------------------------------
    -keep public class * extends android.app.Activity
    -keep public class * extends android.app.Application
    -keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.content.ContentProvider
    -keep public class * extends android.preference.Preference
    -keep public class com.android.vending.licensing.ILicensingService

    #3、
    #-----------------过滤掉自己编写的实体类-------------------------------------------------------------------
    #-keep class com.classtc.test.entity.**{*;}

    #4、
    #----------------保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在------------------------------------
    -keepclasseswithmembernames class * {
        public <init>(android.content.Context, android.util.AttributeSet);
    }

    -keepclasseswithmembernames class * {
        public <init>(android.content.Context, android.util.AttributeSet, int);
    }