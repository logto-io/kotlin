package io.logto.client.utils

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import io.logto.client.exception.LogtoException
import org.jose4j.base64url.Base64Url
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object GenerateUtils {
    private const val CODE_VERIFIER_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
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
            throw LogtoException(
                "${LogtoException.ENCRYPT_ALGORITHM_NOT_SUPPORTED}: $DEFAULT_ALGORITHM",
                exception,
            )
        } catch (exception: UnsupportedEncodingException) {
            throw LogtoException(LogtoException.CODE_CHALLENGE_ENCODED_FAILED, exception)
        }
    }

    fun generateState(): String {
        return generateRandomString()
    }

    private fun generateRandomString(stringLength: Int = DEFAULT_RANDOM_STRING_LENGTH): String {
        val randomString = NanoIdUtils.randomNanoId(
            NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
            CODE_VERIFIER_ALPHABET.toCharArray(),
            stringLength
        )
        return Base64Url.encode(randomString.toByteArray())
    }
}
