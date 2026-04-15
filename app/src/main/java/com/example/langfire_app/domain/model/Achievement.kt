package com.example.langfire_app.domain.model

data class Achievement(
    val id: Int = 0,
    val type: String,
    val value: Int? = null,
    val unlocked: Boolean = false,
    val description: String? = null,
    val icon: String = "",
    val title: String = "",
    val profileId: Int
)
