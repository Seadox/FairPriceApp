package com.seadox.fairprice.utils

import android.util.Base64

class Utils {
    companion object {
        fun isUrl(input: String): Boolean {
            val urlRegex = Regex("^https?://\\S+$")
            return urlRegex.matches(input)
        }

        fun base64ToByteArray(base64String: String): ByteArray {
            val cleanBase64 = base64String.substringAfter("base64,", base64String)
            return Base64.decode(cleanBase64, Base64.DEFAULT)
        }

    }
}