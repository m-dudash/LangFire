package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.UnitWordItem

interface UnitDetailsRepository {

    suspend fun getUnitName(unitId: Int): String?
    suspend fun getWordsForUnit(unitId: Int, profileId: Int): List<UnitWordItem>
    suspend fun countToLearnByCourse(profileId: Int, courseId: Int, nowMs: Long): Int
    suspend fun saveWordMark(profileId: Int, wordId: Int, coeff: Float)
    suspend fun clearWordProgress(profileId: Int, wordId: Int)
}
