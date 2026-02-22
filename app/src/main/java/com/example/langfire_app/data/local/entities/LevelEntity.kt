package com.example.langfire_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level")
data class LevelEntity(
    @PrimaryKey val id: Int,
    val name: String
)