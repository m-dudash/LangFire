package com.example.langfire_app.domain.repository

interface AppSeeder {

    suspend fun seedForProfile(profileId: Int)
    suspend fun seedRulesIfMissing()
}
