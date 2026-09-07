package com.sonms.aishortcut.core.translate

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.mlkit.nl.translate.Translator as MlKitClient

actual fun createTranslator(): Translator = MlKitTranslator()

// ponytail: one English->Korean client for the whole process, created on first
// use and never closed -- process death is the only cleanup it needs. Add
// close() / other language pairs when a second target language shows up.
private class MlKitTranslator : BatchTranslator() {

    private val client: MlKitClient by lazy {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.KOREAN)
                .build(),
        )
    }

    private var modelReady = false

    override suspend fun translateOne(text: String): String {
        if (!modelReady) {
            // Wi-Fi only: the Korean model is ~30MB, not worth a user's mobile data.
            client.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build()).await()
            modelReady = true
        }
        return client.translate(text).await()
    }
}

// Task -> suspend. Kept local rather than pulling in
// kotlinx-coroutines-play-services for two call sites.
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}
