package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "course",
    foreignKeys = [
        ForeignKey(entity = LanguageEntity::class, parentColumns = ["id"], childColumns = ["target_language_id"])
    ]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "target_lang") val targetLang: String,
    @ColumnInfo(name = "icon") val icon: String,
    @ColumnInfo(name = "target_language_id") val targetLanguageId: Int = 1,

)