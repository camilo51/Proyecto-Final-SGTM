package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.common.requireData

open class
UserRepository(
    private val apiService: ApiService
) {

    open suspend fun getUsers(): List<User> {
        return apiService.getUsers().requireData()
    }

    open suspend fun getUser(id: String): User {
        return apiService.getUser(id)
    }

    open suspend fun createUser(user: User): User {
        return apiService.createUser(user)
    }

    open suspend fun updateUser(
        id: String,
        user: User
    ): User {
        return apiService.updateUser(id, user).requireData()
    }

    open suspend fun deleteUser(id: String) {
        apiService.deleteUser(id)
    }
}
