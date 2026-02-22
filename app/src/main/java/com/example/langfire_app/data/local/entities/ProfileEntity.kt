package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val xp: Int,
    @ColumnInfo(name = "streak_days") val streakDays: Int,
    @ColumnInfo(name = "last_active_date") val lastActiveDate: Long
)