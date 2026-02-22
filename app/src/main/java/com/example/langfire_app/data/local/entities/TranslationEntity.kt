package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "translation",
    foreignKeys = [
        ForeignKey(entity = WordsEntity::class, parentColumns = ["id"], childColumns = ["words_id_primary"]),
        ForeignKey(entity = WordsEntity::class, parentColumns = ["id"], childColumns = ["words_id_secondary"])
    ]
)
data class TranslationEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "words_id_primary") val wordsIdPrimary: Int,
    @ColumnInfo(name = "words_id_secondary") val wordsIdSecondary: Int,
    @ColumnInfo(name = "example_sentence") val exampleSentence: String?
)