<p align="center">
    <a href="https://logto.io" target="_blank" align="center" alt="Logto Logo">
        <img src="./logo.png" width="100">
    </a>
    <br/>
    <span><i><a href="https://logto.io" target="_blank">Logto</a> helps you build the sign-in experience and user identity within minutes.</i></span>
</p>

# Logto Kotlin SDKs
[![Maven Central](https://img.shields.io/maven-central/v/io.logto.sdk/kotlin?logo=android)](https://search.maven.org/artifact/io.logto.sdk/android)
[![Build Status](https://github.com/logto-io/kotlin/actions/workflows/main.yml/badge.svg)](https://github.com/logto-io/kotlin/actions/workflows/main.yml)
[![Codecov](https://img.shields.io/codecov/c/github/logto-io/kotlin)](https://app.codecov.io/gh/logto-io/kotlin?branch=master)


The monorepo for SDKs written in Kotlin.

Check out the [Android SDK tutorial](https://docs.logto.io/sdk/android) for more information.

## Installation
Logto Android SDK is now available on [MavenCentral](https://search.maven.org/search?q=io.logto.sdk).

### Groovy
```groovy
implementation 'io.logto.sdk:android:<version>'
```
### Kotlin
```kotlin
implementation("io.logto.sdk:android:<version>")
```

### Configure the redirect scheme

The sign-in experience opens in a [Custom Tab](https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs)
(the system browser), so WebAuthn/passkeys and the browser session work out of the box.
The OAuth redirect is routed back to your app through an intent filter, and you must declare
its scheme with the `logtoRedirectScheme` manifest placeholder in your app's `build.gradle(.kts)`.

The placeholder is the custom scheme of the redirect URI passed to `signIn` / `signOut`
(lowercase, reverse-DNS style). The redirect URI follows the pattern
`$(scheme)://$(applicationId)/callback`, e.g. `io.logto.android://io.logto.sample/callback` —
the host is bound to your `applicationId` by the SDK, the path is fixed to `/callback`,
and both are enforced by Android's intent filter matching.

#### Groovy
```groovy
android {
    defaultConfig {
        manifestPlaceholders.logtoRedirectScheme = 'io.logto.android'
    }
}
```

#### Kotlin
```kotlin
android {
    defaultConfig {
        manifestPlaceholders["logtoRedirectScheme"] = "io.logto.android"
    }
}
```

Prefer an `https` redirect URI bound to a domain you own? See
[Use App Links](#use-app-links-instead-of-a-custom-scheme) below.

### Use App Links instead of a custom scheme

Custom schemes have no ownership: any app can declare the same scheme and race for the
redirect. [Android App Links](https://developer.android.com/training/app-links) bind an
`https` redirect URI to a domain you own through a verified
[Digital Asset Links](https://developers.google.com/digital-asset-links/v1/getting-started)
file, so the OS guarantees only your app receives the redirect. This is the redirect
option recommended for native apps by
[RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252#section-7.2).

The SDK is scheme-agnostic at runtime, and the fully qualified name of the redirect
receiver activity, `io.logto.sdk.android.auth.logto.LogtoRedirectReceiverActivity`, is
part of the public API — declare additional intent filters on it in your app's manifest
and they are merged into the SDK's declaration.

1. Host the Digital Asset Links file at
   `https://your.domain/.well-known/assetlinks.json`, declaring your application id and
   the SHA-256 fingerprints of your signing certificates. When publishing with Play App
   Signing, the release fingerprint comes from Play Console → Setup → App signing. The
   file must be served as `Content-Type: application/json` with HTTP 200 and no
   redirects.

2. Declare the App Links intent filter on the SDK's receiver activity in your app's
   `AndroidManifest.xml`. If you do not use the custom scheme at all, drop the SDK's
   built-in filter with `tools:node="removeAll"` — the `logtoRedirectScheme` placeholder
   is then no longer required:

   ```xml
   <manifest xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:tools="http://schemas.android.com/tools">
       <application>
           <activity android:name="io.logto.sdk.android.auth.logto.LogtoRedirectReceiverActivity">
               <!-- Omit this line to keep the custom-scheme redirect working alongside. -->
               <intent-filter tools:node="removeAll" />
               <intent-filter android:autoVerify="true">
                   <action android:name="android.intent.action.VIEW" />
                   <category android:name="android.intent.category.DEFAULT" />
                   <category android:name="android.intent.category.BROWSABLE" />
                   <data
                       android:scheme="https"
                       android:host="your.domain"
                       android:path="/callback" />
               </intent-filter>
           </activity>
       </application>
   </manifest>
   ```

3. Register `https://your.domain/callback` as a redirect URI (and, if used for
   sign-out, a post sign-out redirect URI) in the Logto console, and pass it to
   `signIn` / `signOut`.

Keep in mind that the callback is now a real URL on your domain: serve a fallback page
there (e.g. a "Return to app" button) for browsers that do not launch App Links on a
server redirect. On Android 12+ an unverified domain never opens the app, so a broken
`assetlinks.json` fails silently — check the verification state with
`adb shell pm get-app-links <applicationId>`.

> Upgrading from v2? See [MIGRATION.md](./MIGRATION.md) for the breaking changes
> (WebView removal, WeChat/Alipay native sign-in removal, and more).

## Products
| Name | Description |
|---|---|
| Kotlin SDK | Kotlin SDK is used to integrate your JVM client with Logto service |
| Android SDK | Android SDK |

## Contributing

After cloning the repository, install the git hooks once so detekt runs `--auto-correct` on staged Kotlin files before each commit:

```bash
./gradlew installGitHooks
```

## Releasing
See [RELEASE.md](./RELEASE.md) for the automated release flow, required secrets, and manual fallback.

## Resources

[![Website](https://img.shields.io/badge/website-logto.io-8262F8.svg)](https://logto.io/)
[![Docs](https://img.shields.io/badge/docs-logto.io-green.svg)](https://docs.logto.io/)
[![Discord](https://img.shields.io/discord/965845662535147551?logo=discord&logoColor=ffffff&color=7389D8&cacheSeconds=600)](https://discord.gg/UEPaF3j5e6)
