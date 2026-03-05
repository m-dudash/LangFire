package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_progress",
    foreignKeys = [
        ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profile_id"]),
        ForeignKey(entity = WordsEntity::class, parentColumns = ["id"], childColumns = ["word_id"])
    ]
)
data class WordProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "knowledge_coeff") val knowledgeCoeff: Float?,
    @ColumnInfo(name = "last_reviewed") val lastReviewed: Long?,
    @ColumnInfo(name = "correct_count") val correctCount: Int?,
    @ColumnInfo(name = "incorrect_count") val incorrectCount: Int?,
    @ColumnInfo(name = "profile_id") val profileId: Int,
    @ColumnInfo(name = "word_id") val wordId: Int,

    // ── SM-2 Spaced Repetition fields ──────────────────────────────────────
    /** How many days until the next review (grows after each correct answer). */
    @ColumnInfo(name = "srs_interval") val srsInterval: Int = 0,
    /** Ease factor (SM-2 EF), starts at 2.5, min 1.3. */
    @ColumnInfo(name = "srs_ease_factor") val srsEaseFactor: Float = 2.5f,
    /** Number of consecutive correct answers (resets to 0 on "Forgot"). */
    @ColumnInfo(name = "srs_repetition") val srsRepetition: Int = 0,
    /** Epoch-ms timestamp: when this word should be reviewed next (0 = now/immediately). */
    @ColumnInfo(name = "next_review_at") val nextReviewAt: Long = 0L
)