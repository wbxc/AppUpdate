# AppUpdate
1、App应用升级

2、代码混淆

在prguard-rules.pro文件中写的，其实就是混淆规则，规定哪些东西不需要混淆。
自己编写的代码中大致就是一些重要的类需要混淆，而混淆的本质就是精简类名，
用简单的a,b,c等单词来代替之前写的如DataUtil等易懂的类名。

所以，理解了这点，也就好理解这个混淆文件该怎么写了。
大致思路就是：
    <1>不混淆第三方库，
    <2>不混淆系统组件，
    <3>一般也可以不混淆Bean等模型类
    因为这些对别人都是没用的，毕竟都是开源的。。。


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

    -keep class org.apache.**{*;}                                               #过滤commons-httpclient-3.1.jar

    -keep class com.fasterxml.jackson.**{*;}                                    #过滤jackson-core-2.1.4.jar等

    -dontwarn com.lidroid.xutils.**                                             #去掉警告
    -keep class com.lidroid.xutils.**{*;}                                       #过滤xUtils-2.6.14.jar
    -keep class * extends java.lang.annotation.Annotation{*;}                   #这是xUtils文档中提到的过滤掉注解

    -dontwarn com.baidu.**                                                      #去掉警告
    -dontwarn com.baidu.mapapi.**
    -keep class com.baidu.** {*;}                                               #过滤BaiduLBS_Android.jar
    -keep class vi.com.gdi.bgl.android.**{*;}
    -keep class com.baidu.platform.**{*;}
    -keep class com.baidu.location.**{*;}
    -keep class com.baidu.vi.**{*;}

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
    -keep class com.classtc.test.entity.**{*;}

    #4、
    #----------------保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在------------------------------------
    -keepclasseswithmembernames class * {
        public <init>(android.content.Context, android.util.AttributeSet);
    }

    -keepclasseswithmembernames class * {
        public <init>(android.content.Context, android.util.AttributeSet, int);
    }
