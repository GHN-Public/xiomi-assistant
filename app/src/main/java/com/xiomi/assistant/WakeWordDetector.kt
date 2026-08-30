package com.xiomi.assistant

import android.util.Log

class WakeWordDetector(private val wakeWord: String) {
    
    companion object {
        private const val TAG = "WakeWordDetector"
    }

    fun isWakeWord(spokenText: String): Boolean {
        val normalized = spokenText.trim().toLowerCase()
        val wakeNormalized = wakeWord.toLowerCase()
        
        val similarity = calculateSimilarity(normalized, wakeNormalized)
        
        Log.d(TAG, "Checking: '$normalized' vs '$wakeNormalized', similarity: $similarity")
        
        return similarity > 0.6
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (longer == s1) s2 else s1
        
        if (longer.length == 0) return 1.0
        
        val editDistance = getEditDistance(longer, shorter)
        return (longer.length - editDistance).toDouble() / longer.length
    }

    private fun getEditDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1) { it }
        
        for (i in 1..s1.length) {
            var nw = i - 1
            costs[0] = i
            
            for (j in 1..s2.length) {
                val cj = minOf(
                    1 + costs[j],
                    1 + costs[j - 1],
                    nw + if (s1[i - 1] == s2[j - 1]) 0 else 1
                )
                nw = costs[j]
                costs[j] = cj
            }
        }
        
        return costs[s2.length]
    }
}
