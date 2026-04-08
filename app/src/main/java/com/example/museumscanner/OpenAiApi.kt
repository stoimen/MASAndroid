package com.example.museumscanner

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAiApi {
    private val client = OkHttpClient()
    private val endpoint = "https://api.openai.com/v1/responses"

    suspend fun describeArtwork(base64Image: String, styleInstruction: String): String {
        val key = BuildConfig.OPENAI_API_KEY
        require(key.isNotBlank()) {
            "OpenAI API key is missing. Add it as OPENAI_API_KEY in app/build.gradle.kts."
        }

        val prompt = """
            You are a museum guide. Analyze the provided artwork image.
            Provide:
            1) What is likely represented
            2) Artist/author (or likely artist/period if uncertain)
            3) Art style and why
            4) One short interesting fact
            Output in plain text and keep it around 120-180 words.
            Additional style request from user: $styleInstruction
        """.trimIndent()

        val bodyJson = JSONObject()
            .put("model", "gpt-4.1-mini")
            .put("input", JSONArray().put(
                JSONObject().put("role", "user").put("content", JSONArray()
                    .put(JSONObject().put("type", "input_text").put("text", prompt))
                    .put(
                        JSONObject()
                            .put("type", "input_image")
                            .put("image_url", "data:image/jpeg;base64,$base64Image")
                    )
                )
            ))

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenAI request failed (${response.code}): $raw")
            }

            val json = JSONObject(raw)
            val output = json.optJSONArray("output") ?: return raw

            for (i in 0 until output.length()) {
                val item = output.optJSONObject(i) ?: continue
                val content = item.optJSONArray("content") ?: continue
                for (j in 0 until content.length()) {
                    val contentItem = content.optJSONObject(j) ?: continue
                    if (contentItem.optString("type") == "output_text") {
                        return contentItem.optString("text")
                    }
                }
            }

            return raw
        }
    }
}
