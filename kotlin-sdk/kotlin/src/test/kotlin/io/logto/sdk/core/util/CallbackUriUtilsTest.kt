package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.exception.CallbackUriVerificationException
import org.junit.Assert
import org.junit.Test

class CallbackUriUtilsTest {
    @Test
    fun `verifyAndParseCodeFromCallbackUri should get expected code without exception`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should get excepted code from the URI which has a custom scheme`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "io.logto.android://io.logto.sample/callback"
        val callbackUri = "io.logto.android://io.logto.sample/callback?state=$state&code=$code"
        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should get expected code from custom scheme URI without authority`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "io.logto.android:/callback"
        val callbackUri = "io.logto.android:/callback?state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should get expected code from custom scheme URI without path`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "io.logto.android://io.logto.sample"
        val callbackUri = "io.logto.android://io.logto.sample?state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should decode query parameters`() {
        val state = "test state"
        val code = "test/code"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=test%20state&code=test%2Fcode"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with mismatched URI`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://yourapp.com/callback?state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.URI_MISMATCHED.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw when callback path is only a prefix match`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback2?state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.URI_MISMATCHED.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should match redirect URI query parameters`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback?connector_id=foo"
        val callbackUri = "https://myapp.com/callback?connector_id=foo&state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should normalize URI scheme host and path`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://MYAPP.com/callback%7E"
        val callbackUri = "HTTPS://myapp.com/callback~?state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUriShould throw with empty URI`() {
        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri("", "", "dummyState")
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with invalid URI format`() {
        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri("invalidUri", "invalidUri", "dummyState")
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with duplicate query parameters`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state&state=anotherState&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with opaque URI`() {
        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(
                "io.logto.android:callback",
                "io.logto.android:callback",
                "dummyState",
            )
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw if callback URI contains fragment`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state&code=$code#fragment"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw if redirect URI contains fragment`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback#fragment"
        val callbackUri = "https://myapp.com/callback?state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.INVALID_URI_FORMAT.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with error parameter`() {
        val state = GenerateUtils.generateState()
        val code = "dummyCode"
        val error = "error"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?error=$error&state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException.error).isEqualTo(error)
        assertThat(expectedException.errorDesc).isNull()
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with error desc with both error and errorDesc parameter`() {
        val state = GenerateUtils.generateState()
        val code = "dummyCode"
        val errorDesc = "you have an error description"
        val encodedErrorDesc = "you%20have%20an%20error%20description"
        val error = "error"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback" +
            "?error_description=$encodedErrorDesc&error=$error&state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException.error).isEqualTo(error)
        assertThat(expectedException.errorDesc).isEqualTo(errorDesc)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with mismatched state`() {
        val state1 = GenerateUtils.generateState()
        val state2 = GenerateUtils.generateState()
        val testCode = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state1&code=$testCode"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state2)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.STATE_MISMATCHED.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with missing state parameter`() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.MISSING_STATE_URI_PARAMETER.name)
    }

    @Test
    fun `verifyAndParseCodeFromCallbackUri should throw with missing code parameter`() {
        val state = GenerateUtils.generateState()
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Type.MISSING_CODE_URI_PARAMETER.name)
    }
}
