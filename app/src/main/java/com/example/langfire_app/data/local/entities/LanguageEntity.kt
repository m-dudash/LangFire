package com.example.langfire_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language")
data class LanguageEntity(
    @PrimaryKey val id: Int,
    val language: String
)