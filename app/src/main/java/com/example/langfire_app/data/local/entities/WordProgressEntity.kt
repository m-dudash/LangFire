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
    @ColumnInfo(name = "word_id") val wordId: Int
)