package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit",
    foreignKeys = [
        ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["course_id"]),
        ForeignKey(entity = LevelEntity::class, parentColumns = ["id"], childColumns = ["level_id"])
    ]
)
data class UnitEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "level_id") val levelId: Int
)