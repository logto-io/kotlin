package io.logto.sdk.core

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.QueryKey
import io.logto.sdk.core.exception.UriConstructionException
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert
import org.junit.Test

class CoreTest {

    @Test
    fun generateSignOutUriWithBothIdTokenAndPostLogoutRedirectUri() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val idToken = "idToken"
        val postLogoutRedirectUri = "https://myapp.com/logout_callback"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, idToken, postLogoutRedirectUri)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.ID_TOKEN_HINT)).isEqualTo(idToken)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(postLogoutRedirectUri)
    }

    @Test
    fun generateSignOutUriWithoutPostLogoutRedirectUri() {
        val endSessionEndpoint = "https://logto.dev/oidc/endSession"
        val idToken = "idToken"

        val resultUri = Core.generateSignOutUri(endSessionEndpoint, idToken)

        val constructedUri = resultUri.toHttpUrl()
        assertThat(constructedUri.scheme).isEqualTo(endSessionEndpoint.toHttpUrl().scheme)
        assertThat(constructedUri.host).isEqualTo(endSessionEndpoint.toHttpUrl().host)
        assertThat(constructedUri.pathSegments).isEqualTo(endSessionEndpoint.toHttpUrl().pathSegments)
        assertThat(constructedUri.queryParameter(QueryKey.ID_TOKEN_HINT)).isEqualTo(idToken)
        assertThat(constructedUri.queryParameter(QueryKey.POST_LOGOUT_REDIRECT_URI)).isEqualTo(null)
    }

    @Test
    fun generateSignOutUriShouldThrowWithInvalidEndpoint() {
        val endSessionEndpoint = "invalid_endpoint"
        val idToken = "idToken"

        val expectedException = Assert.assertThrows(UriConstructionException::class.java) {
            Core.generateSignOutUri(endSessionEndpoint, idToken)
        }

        assertThat(expectedException).hasMessageThat().contains(UriConstructionException.Message.INVALID_ENDPOINT.name)
    }
}
