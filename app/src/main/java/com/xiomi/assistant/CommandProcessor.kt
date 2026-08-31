package com.xiomi.assistant

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

class CommandProcessor(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "CommandProcessor"
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setSpeechRate(0.9f) // Hạ tốc độ nói một chút để giọng rõ hơn
            }
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }

        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId")
        }
        
        // Delay ngắn để đảm bảo nói xong mới thực hiện lệnh tiếp theo (nếu có)
        if (onComplete != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                onComplete()
            }, 1500)
        }
    }

    fun processCommand(command: String) {
        val cleanCommand = command.lowercase().trim()
        Log.d(TAG, "Processing command: $cleanCommand")

        when {
            cleanCommand.contains("mở tiktok") || cleanCommand.contains("tiktok") -> {
                speak("Đang mở TikTok") {
                    openApp(
                        "com.zhiliaoapp.musically", 
                        "com.ss.android.ugc.trill", 
                        "com.ss.android.ugc.aweme"
                    )
                }
            }
            cleanCommand.contains("mở youtube") || cleanCommand.contains("youtube") -> {
                speak("Đang mở YouTube") {
                    openApp("com.google.android.youtube")
                }
            }
            else -> {
                speak("Đã nhận câu lệnh: $cleanCommand")
            }
        }
    }

    private fun openApp(vararg packageNames: String) {
        val pm = context.packageManager
        var launched = false

        for (pkg in packageNames) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                try {
                    context.startActivity(intent)
                    launched = true
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching package: $pkg", e)
                }
            }
        }

        if (!launched) {
            speak("Không tìm thấy ứng dụng trên thiết bị")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
