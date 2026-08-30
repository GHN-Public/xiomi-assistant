package com.xiomi.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import android.util.Log

class VoiceAssistantService : Service() {
    
    companion object {
        private const val TAG = "VoiceAssistant"
        private const val CHANNEL_ID = "VoiceAssistantChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var wakeWordDetector: WakeWordDetector
    private lateinit var commandProcessor: CommandProcessor
    
    private var isWaitingForCommand = false

    override fun onCreate() {
        super.onCreate()
        
        commandProcessor = CommandProcessor(this)
        wakeWordDetector = WakeWordDetector("xiomi")
        
        initSpeechRecognizer()
        startForegroundServiceWithNotification()
        startListening()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech error: $error")
                    restartListeningDelayed(1000)
                }

                override fun onResults(results: Bundle?) {
                    results?.let {
                        val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val spokenText = matches[0]
                            Log.d(TAG, "Recognized text: $spokenText")
                            
                            if (isWaitingForCommand) {
                                // Đang trong trạng thái chờ lệnh -> Xử lý câu lệnh
                                isWaitingForCommand = false
                                commandProcessor.processCommand(spokenText)
                            } else if (wakeWordDetector.isWakeWord(spokenText)) {
                                // Phát hiện Wake Word -> Chuyển sang chờ nhận câu lệnh
                                isWaitingForCommand = true
                                commandProcessor.speak("Dạ, tôi nghe")
                            }
                        }
                    }
                    restartListeningDelayed(1500)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            }
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting listening", e)
                restartListeningDelayed(2000)
            }
        }
    }

    private fun restartListeningDelayed(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            startListening()
        }, delayMillis)
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Xi Ao Mi Assistant")
            .setContentText("Đang lắng nghe câu lệnh...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        commandProcessor.shutdown()
    }
}
