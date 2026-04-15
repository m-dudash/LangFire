package com.example.langfire_app.di

import com.example.langfire_app.data.local.GamificationSeeder
import com.example.langfire_app.data.repository.UnitDetailsRepositoryImpl
import com.example.langfire_app.data.worker.SyncScheduler
import com.example.langfire_app.domain.repository.AppSeeder
import com.example.langfire_app.domain.repository.SyncManager
import com.example.langfire_app.domain.repository.UnitDetailsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainBindingsModule {

    @Binds
    @Singleton
    abstract fun bindUnitDetailsRepository(
        impl: UnitDetailsRepositoryImpl
    ): UnitDetailsRepository

    @Binds
    @Singleton
    abstract fun bindSyncManager(
        impl: SyncScheduler
    ): SyncManager

    @Binds
    @Singleton
    abstract fun bindAppSeeder(
        impl: GamificationSeeder
    ): AppSeeder
}
