package utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object DetektUtil {
    fun getDetektConfig(resourcePath: String): String {
        val inputStream = requireNotNull(DetektUtil::class.java.getResourceAsStream(resourcePath))
        val stringBuilder = StringBuilder()
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            var charCode: Int
            while (reader.read().also { charCode = it } != -1) {
                stringBuilder.append(charCode.toChar())
            }
        }
        return stringBuilder.toString()
    }
}
