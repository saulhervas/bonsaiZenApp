package com.bonsaizen.bonsaizenapp.data.remote

import com.bonsaizen.bonsaizenapp.data.model.users.User
import com.bonsaizen.bonsaizenapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FireBaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
         val authResult = auth.createUserWithEmailAndPassword(email, password).await()
         val firebaseUser = authResult.user
         val user = firebaseUser?.let {
             User(
                 id = it.uid,
                 email = email)
         }?: throw Exception("User is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}