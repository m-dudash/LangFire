package com.example.langfire_app.domain.repository

import com.example.langfire_app.data.local.dao.SessionWordItem
import com.example.langfire_app.domain.srs.SrsEngine

/**
 * Domain interface for the learn / burn session.
 * Loads a batch of SRS-scheduled words and saves the result of each answer.
 */
interface LearnRepository {

    /**
     * Returns up to [limit] words that are due for review in the given course.
     * Prioritizes: new (unseen) words → overdue → upcoming.
     */
    suspend fun getSessionWords(profileId: Int, courseId: Int, limit: Int = 20): List<SessionWordItem>

    /**
     * Records the user's answer quality for [wordId] and updates SM-2 state in the DB.
     */
    suspend fun recordAnswer(profileId: Int, wordId: Int, quality: SrsEngine.Quality)
}
