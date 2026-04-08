package com.example.museumscanner

import android.graphics.Bitmap

class OpenAiArtworkProvider(
    private val api: OpenAiApi = OpenAiApi()
) : ArtworkAiProvider {
    override suspend fun explainArtwork(bitmap: Bitmap, styleInstruction: String): String {
        val base64Image = bitmap.toBase64Jpeg()
        return api.describeArtwork(base64Image, styleInstruction)
    }
}
