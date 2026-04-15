package com.example.langfire_app.domain.model

data class Behavior(
    val id: Int = 0,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attributes: Map<String, String> = emptyMap(),
    val profileId: Int
)
