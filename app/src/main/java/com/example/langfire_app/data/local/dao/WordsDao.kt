package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.WordsEntity
import com.example.langfire_app.data.local.entities.WordProgressEntity

data class WordWithTranslationAndProgress(
    val wordId: Int,
    val word: String,
    val translation: String,
    val knowledgeCoeff: Float?
)

@Dao
interface WordsDao {

    @Query("""
        SELECT 
            w.id AS wordId,
            w.word AS word,
            COALESCE(wt.word, 'No translation') AS translation,
            wp.knowledge_coeff AS knowledgeCoeff
        FROM words w
        LEFT JOIN translation t ON (t.words_id_primary = w.id OR t.words_id_secondary = w.id)
        LEFT JOIN words wt ON (wt.id = t.words_id_primary OR wt.id = t.words_id_secondary) AND wt.id != w.id
        LEFT JOIN word_progress wp ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE w.unit_id = :unitId
        GROUP BY w.id
        ORDER BY w.id ASC
    """)
    suspend fun getWordsForUnit(unitId: Int, profileId: Int): List<WordWithTranslationAndProgress>

}
