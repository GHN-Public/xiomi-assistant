package com.xiomi.assistant

import android.util.Log

class WakeWordDetector(private val defaultWakeWord: String = "xiomi") {

    companion object {
        private const val TAG = "WakeWordDetector"
    }

    // Danh sách các từ biến thể mà Google Speech Recognizer thường trả về khi phát âm "Xi Ao Mi"
    private val wakeWordVariants = listOf(
        "xiomi", "xiaomi", "xi ao mi", "xi a o mi",
        "siêu mì", "siêu mi", "xô mi", "seo mi",
        "say mi", "sao mi", "xi mi", "xiao mi"
    )

    fun isWakeWord(spokenText: String): Boolean {
        val cleanText = spokenText.lowercase().trim()
        Log.d(TAG, "Checking wake word for: $cleanText")

        // 1. Kiểm tra trực tiếp trong danh sách các từ biến thể
        for (variant in wakeWordVariants) {
            if (cleanText.contains(variant)) {
                Log.d(TAG, "Wake word detected match with: $variant")
                return true
            }
        }

        // 2. Kiểm tra nếu người dùng nói từ khóa chính
        return cleanText.contains(defaultWakeWord.lowercase())
    }
}
