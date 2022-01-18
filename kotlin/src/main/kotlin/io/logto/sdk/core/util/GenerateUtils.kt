package io.logto.sdk.core.util

import io.logto.sdk.core.exception.LogtoException
import org.jose4j.base64url.Base64Url
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object GenerateUtils {
    private const val DEFAULT_ALGORITHM = "SHA-256"
    private const val DEFAULT_RANDOM_STRING_LENGTH = 64

    fun generateCodeVerifier(): String {
        return generateRandomString()
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        try {
            val digester: MessageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM)
            digester.update(codeVerifier.toByteArray(Charsets.UTF_8))
            val byteArray: ByteArray = digester.digest()
            return Base64Url.encode(byteArray)
        } catch (exception: NoSuchAlgorithmException) {
            throw LogtoException.System(
                LogtoException.SystemException.ENCODED_ALGORITHM_NOT_SUPPORTED,
                exception
            )
        } catch (exception: UnsupportedEncodingException) {
            throw LogtoException.System(
                LogtoException.SystemException.ENCODING_NOT_SUPPORTED,
                exception
            )
        }
    }

    fun generateState(): String {
        return generateRandomString()
    }

    private fun generateRandomString(length: Int = DEFAULT_RANDOM_STRING_LENGTH): String {
        val actualLen = if (length < 1) DEFAULT_RANDOM_STRING_LENGTH else length
        val randomString = (1..actualLen).map {
            (UByte.MIN_VALUE.toInt()..UByte.MAX_VALUE.toInt()).random().toChar()
        }.joinToString("")
        return Base64Url.encode(randomString.toByteArray())
    }
}
