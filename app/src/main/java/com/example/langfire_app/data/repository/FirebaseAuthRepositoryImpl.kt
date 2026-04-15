package com.example.langfire_app.data.repository

import com.example.langfire_app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun login(email: String, password: String): Flow<Result<Unit>> = flow {
        val result = suspendCancellableCoroutine<Result<Unit>> { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }
        emit(result)
    }

    override fun register(email: String, password: String): Flow<Result<Unit>> = flow {
        val result = suspendCancellableCoroutine<Result<Unit>> { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }
        emit(result)
    }

    override fun logout() {
        auth.signOut()
    }
}
