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
    val lastActiveDate: Long = System.currentTimeMillis(),
    val xpMultiplier: Int = 1,
    val xpMultiplierExpiresAt: Long? = null,
    val avatarPath: String? = null,
    val streakFreezes: Int = 0
)
