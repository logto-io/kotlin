-keep class io.logto.sdk.core.type.** { *; }

# The Alipay and WeChat SDKs are compileOnly dependencies; apps that don't
# bundle them must not fail R8 full-mode missing-class checks.
-dontwarn com.alipay.sdk.**
-dontwarn com.tencent.mm.opensdk.**
