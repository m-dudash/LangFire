package com.example.langfire_app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Singleton
    @Provides
    fun provideCoroutineScope(@ApplicationScope scope: CoroutineScope): CoroutineScope = scope

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson = com.google.gson.Gson()
}
