package com.example.museumscanner

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                MuseumScannerApp()
            }
        }
    }
}

@Composable
fun MuseumScannerApp() {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var explanation by remember { mutableStateOf("") }
    var languageStyle by remember { mutableStateOf("Explain in a concise, easy-to-understand way") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val client = remember { OpenAiVisionClient() }

    val ttsEngine = remember {
        TextToSpeech(context, null)
    }

    LaunchedEffect(Unit) {
        ttsEngine.language = Locale.US
    }
    DisposableEffect(Unit) {
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            explanation = ""
            errorMessage = null
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                selectedBitmap = BitmapFactory.decodeStream(stream)
                explanation = ""
                errorMessage = null
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Museum Artwork Scanner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Capture or select an artwork image, then get AI explanation of author, story, and style.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                    takePicturePreviewLauncher.launch(null)
                }) {
                    Text("Take Photo")
                }
                Button(onClick = {
                    pickPhotoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Choose Image")
                }
            }

            selectedBitmap?.let { bitmap ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected artwork",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            OutlinedTextField(
                value = languageStyle,
                onValueChange = { languageStyle = it },
                label = { Text("Explanation style") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val bitmap = selectedBitmap ?: return@Button
                    isLoading = true
                    errorMessage = null
                    explanation = ""

                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                client.explainArtwork(bitmap, languageStyle)
                            }
                            explanation = result
                        } catch (t: Throwable) {
                            errorMessage = t.message ?: "Unexpected error while analyzing the artwork."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = selectedBitmap != null && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Analyze Artwork")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }

            errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            if (explanation.isNotBlank()) {
                Text(
                    text = "AI Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = explanation)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        ttsEngine.speak(explanation, TextToSpeech.QUEUE_FLUSH, null, "artwork_explanation")
                    }) {
                        Text("Listen")
                    }
                    Button(onClick = {
                        ttsEngine.stop()
                    }) {
                        Text("Stop Audio")
                    }
                }
            }
        }
    }
}

private class OpenAiVisionClient {
    private val api = OpenAiApi()

    suspend fun explainArtwork(bitmap: Bitmap, styleInstruction: String): String {
        val base64Image = bitmap.toBase64Jpeg()
        return api.describeArtwork(base64Image, styleInstruction)
    }
}

private fun Bitmap.toBase64Jpeg(quality: Int = 90): String {
    val output = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}
