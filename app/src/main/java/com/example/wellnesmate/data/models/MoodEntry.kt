package com.example.wellnesmate.data.models

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a mood journal entry
 */
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val mood: MoodType,
    val emoji: String,
    val notes: String = "",
    val timestamp: Date = Date(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

/**
 * Enum representing different mood types
 */
enum class MoodType(val value: Int, val label: String, val emoji: String) {
    // Positive / Happy
    HAPPY(5, "Happy", "😀"),
    ENERGETIC(5, "Energetic", "😁"),
    CONFIDENT(5, "Confident", "😎"),
    RELAXED(4, "Relaxed", "😌"),
    PEACEFUL(4, "Peaceful", "😇"),

    // Neutral / Mixed
    RELIEVED(3, "Relieved", "😐"),
    THINKING(3, "Thinking", "🤔"),
    TIRED(3, "Tired", "😴"),
    NERVOUS(3, "Nervous", "😬"),
    CONFUSED(3, "Confused", "😕"),

    // Negative / Sad
    SAD(2, "Sad", "😞"),
    ANNOYED(2, "Annoyed", "😤"),
    EXHAUSTED(1, "Exhausted", "😩"),
    ANGRY(1, "Angry", "😡"),
    HEARTBROKEN(1, "Heartbroken", "😭");

    companion object {
        fun fromValue(value: Int): MoodType {
            return values().find { it.value == value } ?: RELAXED
        }
        
        fun fromLabel(label: String): MoodType {
            return values().find { it.label == label } ?: RELAXED
        }
        
        fun getAllMoods(): List<MoodType> {
            // Ordered as 3 rows of 5 (Positive, Neutral, Negative)
            return listOf(
                // Row 1: Positive / Happy
                HAPPY, ENERGETIC, CONFIDENT, RELAXED, PEACEFUL,
                // Row 2: Neutral / Mixed
                RELIEVED, THINKING, TIRED, NERVOUS, CONFUSED,
                // Row 3: Negative / Sad
                SAD, ANNOYED, EXHAUSTED, ANGRY, HEARTBROKEN
            )
        }
    }
}

/**
 * Data class for mood statistics
 */
data class MoodStats(
    val averageMood: Float = 0f,
    val mostFrequentMood: MoodType = MoodType.RELAXED,
    val totalEntries: Int = 0,
    val moodCounts: Map<MoodType, Int> = emptyMap(),
    val weeklyTrend: List<Float> = emptyList() // Average mood for each day of the week
)