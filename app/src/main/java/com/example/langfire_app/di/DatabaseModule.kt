package com.example.langfire_app.di

import android.content.Context
import androidx.room.Room
import com.example.langfire_app.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "langfire_db"
        ).build()
    }

//    @Provides
//    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()
//
//    @Provides
//    fun provideWordsDao(db: AppDatabase): WordsDao = db.wordsDao()
}