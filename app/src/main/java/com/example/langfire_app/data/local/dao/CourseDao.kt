package com.example.langfire_app.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.langfire_app.data.local.entities.CourseEntity

@Dao
interface CourseDao {
    @Query("SELECT * FROM course")
    suspend fun getAll(): List<CourseEntity>

    @Query("SELECT * FROM course WHERE id = :id")
    suspend fun getById(id: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity)
}