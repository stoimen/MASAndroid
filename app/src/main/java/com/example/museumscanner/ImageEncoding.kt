package com.example.museumscanner

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64Jpeg(quality: Int = 90): String {
    val output = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}
