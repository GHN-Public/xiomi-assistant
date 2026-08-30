package com.xiomi.assistant

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class CommandProcessor(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "CommandProcessor"
    }

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isTtsReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun processCommand(command: String) {
        val cleanCommand = command.lowercase().trim()
        Log.d(TAG, "Processing command: $cleanCommand")

        if (cleanCommand.contains("mở tiktok") || cleanCommand.contains("tiktok")) {
            speak("Đang mở TikTok")
            openApp("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
        } else {
            speak("Tôi đã nghe thấy: $cleanCommand")
        }
    }

    private fun openApp(vararg packageNames: String) {
        val pm = context.packageManager
        for (pkg in packageNames) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }
        speak("Không tìm thấy ứng dụng TikTok trên máy")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
