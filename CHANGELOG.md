# Changelog

## [3.0.0-beta](https://github.com/logto-io/kotlin/compare/v2.0.2...v3.0.0-beta) (2026-06-12)


### ⚠ BREAKING CHANGES

* **android-sdk:** revamp the sign-out API ([#251](https://github.com/logto-io/kotlin/issues/251))
* replace WebView sign-in with Chrome Custom Tabs ([#249](https://github.com/logto-io/kotlin/issues/249))

### Features

* **android-sdk:** revamp the sign-out API ([#251](https://github.com/logto-io/kotlin/issues/251)) ([153c780](https://github.com/logto-io/kotlin/commit/153c780adba85a7c8d7f142fefd50f4f1db8a439))
* replace WebView sign-in with Chrome Custom Tabs ([#249](https://github.com/logto-io/kotlin/issues/249)) ([d7aca51](https://github.com/logto-io/kotlin/commit/d7aca512cca0a241f6bfd31a24d3004dab3acbfa))


### Bug Fixes

* **android-sdk:** clear local credentials eagerly on browser sign-out ([#250](https://github.com/logto-io/kotlin/issues/250)) ([f295735](https://github.com/logto-io/kotlin/commit/f295735b0af37c2290568b548aa5872c2e92ae5c))
* **android-sdk:** discard in-flight token responses after sign-out ([#263](https://github.com/logto-io/kotlin/issues/263)) ([6a84601](https://github.com/logto-io/kotlin/commit/6a846011ce3ad77f15d329042186be295decca71))
* **android-sdk:** invalidate unauthenticated sign-out and keep omitted refresh tokens ([#265](https://github.com/logto-io/kotlin/issues/265)) ([7251e40](https://github.com/logto-io/kotlin/commit/7251e40d2e3ad40da5cbddb01a2adcaa022b558a))
* **core:** exercise fetchJwksJson in its failure-path test ([#247](https://github.com/logto-io/kotlin/issues/247)) ([1c79e7b](https://github.com/logto-io/kotlin/commit/1c79e7b26b4919fe80605a34af503b4e9505fbed))
* replace HttpUrl for callback URI parsing ([#244](https://github.com/logto-io/kotlin/issues/244)) ([e345774](https://github.com/logto-io/kotlin/commit/e34577418674d2404ef8dee33de91707c00ea3bc))
