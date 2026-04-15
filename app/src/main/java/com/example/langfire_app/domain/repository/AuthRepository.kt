package com.example.langfire_app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    fun login(email: String, password: String): Flow<Result<Unit>>
    fun register(email: String, password: String): Flow<Result<Unit>>
    fun logout()
}
