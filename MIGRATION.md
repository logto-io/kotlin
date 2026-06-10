# Migrating from v2 to v3

v3 replaces the embedded WebView sign-in with [Chrome Custom Tabs](https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs)
(the system browser). This unlocks WebAuthn/passkey sign-in on Android — WebView forbids
WebAuthn, and [RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252) forbids WebView
for OAuth altogether — and shares the browser session (SSO) with Chrome.

## Required: declare the redirect scheme

In v2 the OAuth redirect was intercepted inside the WebView, so no manifest setup was
needed. In v3 the redirect comes back to the app through an OS-level intent filter.

The redirect URI keeps the v2 pattern
`$(LOGTO_REDIRECT_SCHEME)://$(YOUR_APP_PACKAGE)/callback` — e.g.
`io.logto.android://io.logto.sample/callback` — but v3 turns the whole pattern
into enforced requirements:

The **scheme** must be declared via the `logtoRedirectScheme` manifest placeholder
(a lowercase, reverse-DNS style custom scheme):

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        // Must equal the scheme of the redirect URI passed to `signIn` / `signOut`:
        manifestPlaceholders["logtoRedirectScheme"] = "io.logto.android"
    }
}
```

Without this placeholder the manifest merge fails with
`requires a placeholder substitution but no value for <logtoRedirectScheme> is provided`.

The **host** must be your `applicationId`. The SDK binds the intent filter's host to the
built-in `${applicationId}` placeholder, so Android only routes redirects addressed to
your app — several apps sharing the same scheme no longer collide. In v2 this part of the
pattern was a documented convention; in v3 a redirect URI whose host is not your
`applicationId` is never delivered to the app, so check your registered URIs when
upgrading. Keep both parts lowercase: intent filter matching is case-sensitive, and
browsers lowercase the scheme.

The **path** must be `/callback`. In v2 any path worked; in v3 the intent filter also
constrains the path, so a redirect URI with a different path is never delivered to
the app — update any registered URIs that deviate from the pattern.

The `signIn` call itself is unchanged; deriving the URI from `BuildConfig.APPLICATION_ID`
keeps it correct across build variants that use an `applicationIdSuffix`:

```kotlin
logtoClient.signIn(this, "io.logto.android://${BuildConfig.APPLICATION_ID}/callback") { exception ->
    // ...
}
```

The intent filter narrows routing, but it is not a security boundary — a hostile app can
copy it verbatim. The actual protections are the SDK validating every delivered URI
against the in-flight session and PKCE making an intercepted authorization code useless.
For verified, non-preemptable redirects, you can additionally declare an `https` App Link
intent filter for `io.logto.sdk.android.auth.logto.LogtoRedirectReceiverActivity` in your
own manifest.

## Removed: WeChat / Alipay native sign-in

The native social sign-in handoff relied on the WebView JavaScript bridge, which a
Custom Tab (a sandboxed browser) cannot provide. The `io.logto.sdk.android.auth.social`
package, the `io.logto.sdk:alipay` artifact, and the `ALIPAY_*` / `WECHAT_*`
`LogtoException` types are removed. Apps no longer need a `WXEntryActivity`.

Social connectors themselves keep working: the whole social flow (including redirects to
provider apps via their universal/app links) now happens in the browser, like on the web.

If you depend on WeChat/Alipay **native SDK** sign-in, stay on v2 — it remains available
on Maven Central and receives fixes on the `v2.x` line.

## Changed: sign-out API

In v3, `signOut` performs a complete sign-out: it clears local credentials, revokes the
refresh token, and ends the session on the Logto server by opening the end session
endpoint in the browser. Local credentials are always cleared; the remote steps are
best-effort and need the OIDC config to be reachable — any failure is reported through
the completion.

```kotlin
// The browser opens briefly and navigates back to the app through the post sign-out
// redirect URI (register it in the Logto console first; its scheme must also match
// `logtoRedirectScheme`):
logtoClient.signOut(this, "io.logto.android://io.logto.sample/callback") { exception ->
    // ...
}

// Or omit the URI — no console configuration needed; the browser shows the Logto
// sign-out page and the user dismisses it manually:
logtoClient.signOut(this)
```

Dismissing the browser during sign-out is never reported as `USER_CANCELED`: local
credentials are cleared and the token revocation has settled before the browser opens,
so the sign-out has already taken effect. Failures of the earlier steps (such as a
failed revocation) are still reported through the completion.

The v2 `signOut(completion)` is renamed to `clearCredentials(completion)` with unchanged
behavior: it clears local credentials and revokes the refresh token, but it cannot clear
the session cookie — that cookie now lives in the browser, which the app cannot touch.
After clearing credentials, the next `signIn` may silently re-enter the account through
the existing browser session. Prefer `signOut`; use `clearCredentials` when no UI context
is available (e.g. forcing a local sign-out from a background error handler).

## Other breaking changes

- New `LogtoException.Type.UNABLE_TO_LAUNCH_BROWSER` is reported when no browser is
  available to handle the sign-in.
- If Android kills your app process while the browser is in the foreground (rare), the
  in-flight sign-in is abandoned; initiate the sign-in again.
- Only the most recent sign-in / sign-out flow is tracked: starting a new flow while
  another is pending abandons the previous one (its completion is never invoked).
