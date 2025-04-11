package com.dmu.dmu_app.util

import android.util.Base64
import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SignatureUtil {
    fun generateSignature(
        accessKey: String,
        accessSecret: String,
        timestamp: String,
        signatureVersion: String,
        httpMethod: String = "POST",
        httpUri: String = "/v1/identify",
        dataType: String = "audio"
    ): String {
        val stringToSign = "$httpMethod\n$httpUri\n$accessKey\n$dataType\n$signatureVersion\n$timestamp"
        return try {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(accessSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
            mac.init(secretKeySpec)
            val rawHmac = mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(rawHmac, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            Log.e("SignatureUtil", "Signature generation failed: ${e.message}")
            ""
        }
    }
}