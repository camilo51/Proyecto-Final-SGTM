package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.LoginRequest

class LoginRepository(
    private val apiService: ApiService
){
    suspend fun login(login: LoginRequest) =
        apiService.login(login)
}