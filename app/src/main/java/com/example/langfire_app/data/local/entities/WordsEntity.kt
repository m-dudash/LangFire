package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"]),
        ForeignKey(entity = WordTypeEntity::class, parentColumns = ["id"], childColumns = ["word_type_id"]),
        ForeignKey(entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unit_id"]),
        ForeignKey(entity = LevelEntity::class, parentColumns = ["id"], childColumns = ["level_id"]),
        ForeignKey(entity = LanguageEntity::class, parentColumns = ["id"], childColumns = ["language_id"])
    ]
)
data class WordsEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val plural: String?,
    @ColumnInfo(name = "audio_path") val audioPath: String?,
    @ColumnInfo(name = "article_id") val articleId: Int,
    @ColumnInfo(name = "word_type_id") val wordTypeId: Int,
    @ColumnInfo(name = "unit_id") val unitId: Int,
    @ColumnInfo(name = "level_id") val levelId: Int,
    @ColumnInfo(name = "language_id") val languageId: Int
)