package io.logto.sdk.core.util

import com.google.common.truth.Truth.assertThat
import io.logto.sdk.core.constant.ReservedScope
import org.junit.Test

class ScopeUtilsTest {
    @Test
    fun `withReservedScopes should contain default scopes with null scope param`() {
        val nullScope: List<String>? = null
        val ensuredNullScope = ScopeUtils.withReservedScopes(nullScope)

        assertThat(ensuredNullScope).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
        }
    }

    @Test
    fun `withReservedScopes should contain default scopes with scope param have 'openid' only`() {
        val openidScope = listOf(ReservedScope.OPENID)
        val ensuredOpenidScope = ScopeUtils.withReservedScopes(openidScope)

        assertThat(ensuredOpenidScope).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
        }
    }

    @Test
    fun `withReservedScopes should contain default scopes with scope param have 'offline_access' only`() {
        val offlineAccessScope = listOf(ReservedScope.OFFLINE_ACCESS)
        val ensuredOfflineAccessScope = ScopeUtils.withReservedScopes(offlineAccessScope)

        assertThat(ensuredOfflineAccessScope).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
        }
    }

    @Test
    fun `withReservedScopes should contain all scopes`() {
        val expectedScope = "exceptedScope"
        val normalScope = listOf(ReservedScope.OPENID, ReservedScope.OFFLINE_ACCESS, expectedScope)
        val scopes = ScopeUtils.withReservedScopes(normalScope)

        assertThat(scopes).apply {
            contains(ReservedScope.OPENID)
            contains(ReservedScope.OFFLINE_ACCESS)
            contains(expectedScope)
        }
    }
}
