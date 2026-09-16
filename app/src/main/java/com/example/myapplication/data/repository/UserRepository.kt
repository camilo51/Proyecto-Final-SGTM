package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.common.requireData

class UserRepository(
    private val apiService: ApiService
) {

    suspend fun getUsers(): List<User> {
        return apiService.getUsers().requireData()
    }

    suspend fun getUser(id: String): User {
        return apiService.getUser(id).requireData()
    }

    suspend fun createUser(user: User): User {
        return apiService.createUser(user).requireData()
    }

    suspend fun updateUser(
        id: String,
        user: User
    ): User {
        return apiService.updateUser(id, user).requireData()
    }

    suspend fun deleteUser(id: String) {
        apiService.deleteUser(id)
    }
}