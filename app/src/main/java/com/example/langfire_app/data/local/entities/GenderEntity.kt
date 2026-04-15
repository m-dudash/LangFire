package com.example.langfire_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gender")
data class GenderEntity(
    @PrimaryKey val id: Int,
    val name: String
)