package com.example.langfire_app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit")
data class UnitEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "course_id") val courseId: Int
)