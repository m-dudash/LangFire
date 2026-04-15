package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.SessionWord
import com.example.langfire_app.domain.srs.SrsEngine

interface LearnRepository {

    suspend fun getSessionWords(profileId: Int, courseId: Int, limit: Int = 20): List<SessionWord>
    suspend fun getSessionWordsBounded(profileId: Int, courseId: Int, maxLevelId: Int, limit: Int = 20): List<SessionWord>
    suspend fun getMaxLevelIdForSession(profileId: Int, courseId: Int): Int?
    suspend fun recordAnswer(profileId: Int, wordId: Int, quality: SrsEngine.Quality)
    suspend fun getDistractors(profileId: Int, courseId: Int, excludedWordId: Int, limit: Int = 3): List<SessionWord>
}
