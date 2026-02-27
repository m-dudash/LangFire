package com.example.langfire_app.domain.repository


import com.example.langfire_app.domain.model.ProfileStats

/**
 * Repository interface for gamification statistics.
 * Implementation queries the local DB (word sessions, quiz results, etc.)
 */
interface StatsRepository {
    suspend fun getProfileStats(profileId: Int): ProfileStats
}