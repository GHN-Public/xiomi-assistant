package com.xiomi.assistant

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class CommandProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "CommandProcessor"
    }

    private lateinit var tts: TextToSpeech

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("vi", "VN")
                Log.d(TAG, "TTS initialized")
            }
        }
    }

    fun processCommand(command: String) {
        Log.d(TAG, "Processing command: $command")
        
        when {
            command.contains("mở telegram", ignoreCase = true) -> {
                openApp("org.telegram.messenger")
                speak("Đang mở Telegram")
            }
            command.contains("mở zalo", ignoreCase = true) -> {
                openApp("com.zing.zalo")
                speak("Đang mở Zalo")
            }
            command.contains("mở tiktok", ignoreCase = true) -> {
                openApp("com.ss.android.ugc.tiktok")
                speak("Đang mở TikTok")
            }
            command.contains("mở facebook", ignoreCase = true) -> {
                openApp("com.facebook.katana")
                speak("Đang mở Facebook")
            }
            command.contains("mở youtube", ignoreCase = true) -> {
                openApp("com.google.android.youtube")
                speak("Đang mở YouTube")
            }
            command.contains("mở messenger", ignoreCase = true) -> {
                openApp("com.facebook.orca")
                speak("Đang mở Messenger")
            }
            command.contains("chụp ảnh", ignoreCase = true) -> {
                capturePhoto()
                speak("Chụp ảnh xong")
            }
            command.contains("tăng âm lượng", ignoreCase = true) -> {
                increaseVolume()
                speak("Tăng âm lượng")
            }
            command.contains("giảm âm lượng", ignoreCase = true) -> {
                decreaseVolume()
                speak("Giảm âm lượng")
            }
            command.contains("mở khóa màn hình", ignoreCase = true) -> {
                unlockScreen()
                speak("Mở khóa màn hình")
            }
            else -> {
                speak("Tôi không hiểu lệnh này. Hãy thử lại")
            }
        }
    }

    private fun openApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                speak("Ứng dụng không được cài đặt")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app: $packageName", e)
        }
    }

    private fun capturePhoto() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing photo", e)
        }
    }

    private fun increaseVolume() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun decreaseVolume() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    private fun unlockScreen() {
        try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            val keyguardLock = keyguardManager.newKeyguardLock("TAG")
            keyguardLock.disableKeyguard()
        } catch (e: Exception) {
            Log.e(TAG, "Error unlocking screen", e)
        }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null)
        }
    }

    fun destroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
