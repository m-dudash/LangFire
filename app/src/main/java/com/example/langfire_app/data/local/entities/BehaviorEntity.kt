package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "behavior",
    foreignKeys = [
        ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profile_id"])
    ]
)
data class BehaviorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val timestamp: Long,
    val attributes: String,
    @ColumnInfo(name = "profile_id") val profileId: Int
)