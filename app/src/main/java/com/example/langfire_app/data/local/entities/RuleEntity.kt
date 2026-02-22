package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rule",
    foreignKeys = [
        ForeignKey(entity = AchievementEntity::class, parentColumns = ["id"], childColumns = ["achievement_id"])
    ]
)
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val conditions: String,
    @ColumnInfo(name = "achievement_id") val achievementId: Int
)