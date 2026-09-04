package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.ResetPasswordRequest

class LoginRepository(
    private val apiService: ApiService
) {

    // =========================
    // INICIAR SESIÓN
    // =========================

    suspend fun login(
        request: LoginRequest
    ) = apiService.login(request)


    // =========================
    // REGISTRAR USUARIO
    // =========================

    suspend fun register(
        request: RegisterRequest
    ) = apiService.register(request)


    // =========================
    // RECUPERAR CONTRASEÑA
    // =========================

    suspend fun forgotPassword(
        request: ForgotPasswordRequest
    ) = apiService.forgotPassword(request)


    // =========================
    // RESTABLECER CONTRASEÑA
    // =========================

    suspend fun resetPassword(
        request: ResetPasswordRequest
    ) = apiService.resetPassword(request)
}