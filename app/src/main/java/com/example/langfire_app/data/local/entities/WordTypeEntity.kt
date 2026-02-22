package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_type")
data class WordTypeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "type") val type: String
)