package com.example.museumscanner

import android.graphics.Bitmap

/**
 * Provider abstraction for image-to-text artwork explanations.
 *
 * This keeps UI code provider-agnostic so additional providers (for example Gemini)
 * can be integrated later without changing MainActivity.
 */
interface ArtworkAiProvider {
    suspend fun explainArtwork(bitmap: Bitmap, styleInstruction: String): String
}
