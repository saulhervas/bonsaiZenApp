package com.bonsaizen.bonsaizenapp.data.repository

import com.bonsaizen.bonsaizenapp.data.model.users.User


interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Result<User>

}