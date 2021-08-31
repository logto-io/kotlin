package io.logto.android.utils

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class PkceUtil {
    companion object {
        private const val CODE_ENCODE_FLAG = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

        fun generateCodeVerifier(): String {
            val secureRandom = SecureRandom()
            val codeBytes = ByteArray(64)
            secureRandom.nextBytes(codeBytes)
            return Base64.encodeToString(codeBytes, CODE_ENCODE_FLAG)
        }

        fun generateCodeChallenge(codeVerifier: String): String {
            try {
                val digester: MessageDigest = MessageDigest.getInstance("SHA-256")
                digester.update(codeVerifier.toByteArray(Charsets.ISO_8859_1))
                val byteArray: ByteArray = digester.digest()
                return Base64.encodeToString(byteArray, CODE_ENCODE_FLAG)
            } catch (error: NoSuchAlgorithmException) {
                throw Exception(error)
            } catch (e: UnsupportedEncodingException) {
                throw Exception(error)
            }
        }
    }
}
