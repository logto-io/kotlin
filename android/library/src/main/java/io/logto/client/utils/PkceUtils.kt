package io.logto.client.utils

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import io.logto.client.exception.LogtoException
import org.jose4j.base64url.Base64Url
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object PkceUtils {
    private const val CODE_VERIFIER_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    private const val CODE_VERIFIER_LEN = 64
    private const val DEFAULT_ALGORITHM = "SHA-256"

    fun generateCodeVerifier(): String {
        val randomString = NanoIdUtils.randomNanoId(
            NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
            CODE_VERIFIER_ALPHABET.toCharArray(),
            CODE_VERIFIER_LEN
        )
        return Base64Url.encode(randomString.toByteArray())
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
}
