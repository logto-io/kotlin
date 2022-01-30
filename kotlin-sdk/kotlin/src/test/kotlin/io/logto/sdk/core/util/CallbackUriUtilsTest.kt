package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.exception.CallbackUriVerificationException
import org.junit.Assert
import org.junit.Test

class CallbackUriUtilsTest {
    @Test
    fun verifyAndParseCodeFromCallbackUri() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state&code=$code"

        val resultCode = CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)

        assertThat(resultCode).isEqualTo(code)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithMismatchedUri() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://yourapp.com/callback?state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Message.URI_MISMATCHED.name)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithEmptyUri() {
        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri("", "", "dummyState")
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Message.INVALID_URI_FORMAT.name)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithInvalidUriFormat() {
        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri("invalidUri", "invalidUri", "dummyState")
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Message.INVALID_URI_FORMAT.name)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithErrorParameter() {
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
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithErrorDescWithBothErrorAndErrorDescParameter() {
        val state = GenerateUtils.generateState()
        val code = "dummyCode"
        val errorDesc = "you hava an error description"
        val error = "error"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?error_description=$errorDesc&error=$error&state=$state&code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException.error).isEqualTo(error)
        assertThat(expectedException.errorDesc).isEqualTo(errorDesc)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithMismatchedState() {
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
            .contains(CallbackUriVerificationException.Message.STATE_MISMATCHED.name)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithMissingStateParameter() {
        val state = GenerateUtils.generateState()
        val code = "testCode"
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?code=$code"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Message.MISSING_STATE_URI_PARAMETER.name)
    }

    @Test
    fun verifyAndParseCodeFromCallbackUriShouldThrowWithMissingCodeParameter() {
        val state = GenerateUtils.generateState()
        val redirectUri = "https://myapp.com/callback"
        val callbackUri = "https://myapp.com/callback?state=$state"

        val expectedException = Assert.assertThrows(CallbackUriVerificationException::class.java) {
            CallbackUriUtils.verifyAndParseCodeFromCallbackUri(callbackUri, redirectUri, state)
        }

        assertThat(expectedException)
            .hasMessageThat()
            .contains(CallbackUriVerificationException.Message.MISSING_CODE_URI_PARAMETER.name)
    }
}
