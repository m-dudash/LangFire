package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.HomeCourseStats
import com.example.langfire_app.domain.model.ProfileStats
import com.example.langfire_app.domain.model.StatWord

interface StatsRepository {
    suspend fun getProfileStats(profileId: Int): ProfileStats
    suspend fun getHomeCourseStats(
        profileId: Int,
        courseId: Int
    ): HomeCourseStats

    suspend fun getToLearnWords(profileId: Int, courseId: Int): List<StatWord>
    suspend fun getPracticedWords(profileId: Int, courseId: Int): List<StatWord>
    suspend fun getLearnedWords(profileId: Int, courseId: Int): List<StatWord>
}