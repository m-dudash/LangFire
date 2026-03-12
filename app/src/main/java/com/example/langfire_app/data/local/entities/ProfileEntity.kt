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
    @ColumnInfo(name = "last_active_date") val lastActiveDate: Long,
    @ColumnInfo(name = "xp_multiplier") val xpMultiplier: Int = 1,
    @ColumnInfo(name = "xp_multiplier_expires_at") val xpMultiplierExpiresAt: Long? = null,
    @ColumnInfo(name = "avatar_path") val avatarPath: String? = null
)