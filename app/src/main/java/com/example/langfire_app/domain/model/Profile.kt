package com.example.langfire_app.domain.model

/**
 * Domain model representing a user profile.
 * Independent of any framework or database implementation.
 */
data class Profile(
    val id: Int = 0,
    val name: String,
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastActiveDate: Long = System.currentTimeMillis()
)
