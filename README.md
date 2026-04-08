# Museum Scanner (Android)

Museum Scanner is a Kotlin Android app that helps you explore museum art pieces.

With the app you can:
- Open the app and quickly **take a photo** of an artwork, or choose one from your gallery.
- Send the image to your **OpenAI API account** for analysis.
- Receive a clear explanation including:
  - what the artwork likely represents,
  - who the author/artist is (or likely author/period),
  - which artistic style it belongs to and why,
  - a short interesting fact.
- Tap **Listen** to hear the explanation while walking through the museum room (Android Text-to-Speech).

---

## Architecture overview

The app is intentionally simple, with one Android module (`app`) and a small set of classes:

- `MainActivity.kt`
  - Jetpack Compose screen and UI state.
  - Camera/gallery image pick.
  - Calls OpenAI analysis service.
  - Text-to-Speech playback controls.
- `OpenAiApi.kt`
  - Builds and sends a request to the OpenAI `responses` API endpoint.
  - Parses AI text from the API response.

---

## Requirements

- Android Studio (latest stable recommended)
- Android SDK 34+
- JDK 17
- OpenAI API key with access to vision-capable models
- Android device (or emulator) running Android 8.0+ (API 26)

---

## Configure your OpenAI key

For local development, add your key in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // ...
    buildConfigField("String", "OPENAI_API_KEY", "\"YOUR_OPENAI_API_KEY\"")
}
```

> Important: do not commit real keys to Git. For production, use a secure backend/token exchange pattern.

---

## Build and run on Android device

### Option 1: Android Studio
1. Open the project folder in Android Studio.
2. Let Gradle sync finish.
3. Connect your Android phone with USB debugging enabled.
4. Select your device from the run target list.
5. Press **Run**.

### Option 2: command line
From the repository root:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Then open **Museum Scanner** on your device.

---

## How to use the app

1. Launch the app.
2. Tap **Take Photo** (camera) or **Choose Image** (gallery).
3. Optionally adjust the explanation style text.
4. Tap **Analyze Artwork**.
5. Read the AI explanation.
6. Tap **Listen** to hear the explanation, and **Stop Audio** to stop playback.

---

## Notes and limitations

- Artist detection can be uncertain when the photo quality is low or metadata is absent.
- API responses depend on model capability and prompt quality.
- Network access is required.
- Camera permission is requested for taking photos.

---

## Future improvements

- Move API calls to a secure backend (recommended for production).
- Save scan history and favorites.
- Add multilingual narration.
- Add offline caching for previously analyzed artworks.

