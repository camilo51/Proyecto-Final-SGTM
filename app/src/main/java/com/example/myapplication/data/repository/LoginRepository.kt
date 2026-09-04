package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.ForgotPasswordResponse
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.RegisterResponse
import com.example.myapplication.data.model.ResetPasswordRequest
import com.example.myapplication.data.model.ResetPasswordResponse

class LoginRepository(
    private val apiService: ApiService
) {

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     * @return LoginResponse si el servidor responde (incluso si es error 4xx/5xx Retrofit lanzará HttpException).
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        return apiService.login(request)
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    suspend fun register(request: RegisterRequest): RegisterResponse {
        return apiService.register(request)
    }

    /**
     * Envía una solicitud de recuperación de contraseña.
     */
    suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
        return apiService.forgotPassword(request)
    }

    /**
     * Restablece la contraseña utilizando el token.
     */
    suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        return apiService.resetPassword(request)
    }
}
