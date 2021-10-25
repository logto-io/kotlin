package io.logto.android.pkce

import android.util.Base64
import io.logto.android.exception.LogtoException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object Util {
    private const val CODE_VERIFIER_LENGTH = 64
    private const val DEFAULT_ALGORITHM = "SHA-256"
    private const val CODE_ENCODE_FLAG = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val codeBytes = ByteArray(CODE_VERIFIER_LENGTH)
        secureRandom.nextBytes(codeBytes)
        return Base64.encodeToString(codeBytes, CODE_ENCODE_FLAG)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        try {
            val digester: MessageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM)
            digester.update(codeVerifier.toByteArray(Charsets.ISO_8859_1))
            val byteArray: ByteArray = digester.digest()
            return Base64.encodeToString(byteArray, CODE_ENCODE_FLAG)
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
