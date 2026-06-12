package io.logto.sdk.core.util

import org.jose4j.base64url.Base64Url
import java.security.MessageDigest

object GenerateUtils {
    private const val DEFAULT_ALGORITHM = "SHA-256"
    private const val DEFAULT_RANDOM_STRING_LENGTH = 64

    fun generateCodeVerifier(): String {
        return generateRandomString()
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val digester: MessageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM)
        digester.update(codeVerifier.toByteArray(Charsets.UTF_8))
        val byteArray: ByteArray = digester.digest()
        return Base64Url.encode(byteArray)
    }

    fun generateState(): String {
        return generateRandomString()
    }

    private fun generateRandomString(length: Int = DEFAULT_RANDOM_STRING_LENGTH): String {
        val randomUnit8Arr = (1..length).map {
            (UByte.MIN_VALUE.toInt()..UByte.MAX_VALUE.toInt()).random().toByte()
        }.toByteArray()
        return Base64Url.encode(randomUnit8Arr)
    }
}
