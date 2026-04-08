package com.example.museumscanner

object AiProviderFactory {
    fun createDefault(): ArtworkAiProvider = OpenAiArtworkProvider()
}
