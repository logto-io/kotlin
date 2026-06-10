# Migrating from v2 to v3

v3 replaces the embedded WebView sign-in with [Chrome Custom Tabs](https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs)
(the system browser). This unlocks WebAuthn/passkey sign-in on Android — WebView forbids
WebAuthn, and [RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252) forbids WebView
for OAuth altogether — and shares the browser session (SSO) with Chrome.

## Required: declare the redirect scheme

In v2 the OAuth redirect was intercepted inside the WebView, so no manifest setup was
needed. In v3 the redirect comes back to the app through an OS-level intent filter, and
your app must declare its scheme via the `logtoRedirectScheme` manifest placeholder:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        // Must equal the scheme of the redirect URI passed to `signIn` / `signOut`,
        // e.g. for the redirect URI "io.logto.android://io.logto.sample/callback":
        manifestPlaceholders["logtoRedirectScheme"] = "io.logto.android"
    }
}
```

Without this placeholder the manifest merge fails with
`requires a placeholder substitution but no value for <logtoRedirectScheme> is provided`.

The `signIn` call itself is unchanged:

```kotlin
logtoClient.signIn(this, "io.logto.android://io.logto.sample/callback") { exception ->
    // ...
}
```

Use a unique, reverse-DNS style scheme that other apps are unlikely to claim. If you need
verified redirects, you can additionally declare an `https` App Link intent filter for
`io.logto.sdk.android.auth.logto.LogtoRedirectReceiverActivity` in your own manifest.

## Removed: WeChat / Alipay native sign-in

The native social sign-in handoff relied on the WebView JavaScript bridge, which a
Custom Tab (a sandboxed browser) cannot provide. The `io.logto.sdk.android.auth.social`
package, the `io.logto.sdk:alipay` artifact, and the `ALIPAY_*` / `WECHAT_*`
`LogtoException` types are removed. Apps no longer need a `WXEntryActivity`.

Social connectors themselves keep working: the whole social flow (including redirects to
provider apps via their universal/app links) now happens in the browser, like on the web.

If you depend on WeChat/Alipay **native SDK** sign-in, stay on v2 — it remains available
on Maven Central and receives fixes on the `v2.x` line.

## Changed: sign-out behavior

`signOut(completion)` still clears local credentials and revokes the refresh token, but it
can no longer clear the session cookie — that cookie now lives in the browser, which the
app cannot touch. After a local-only sign-out, the next `signIn` may silently re-enter the
account through the existing browser session.

To also end the session on the Logto server, use the new overload, which opens the end
session endpoint in the browser and returns through your post sign-out redirect URI
(register it in the Logto console first; its scheme must also match
`logtoRedirectScheme`):

```kotlin
logtoClient.signOut(this, "io.logto.android://io.logto.sample/callback") { exception ->
    // ...
}
```

## Other breaking changes

- New `LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER` is reported when no browser is
  available to handle the sign-in.
- If Android kills your app process while the browser is in the foreground (rare), the
  in-flight sign-in is abandoned; initiate the sign-in again.
- Only one sign-in / sign-out flow can be in flight at a time (unchanged from v2, but now
  enforced across the browser round-trip).
