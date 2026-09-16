package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.ForgotPasswordResponse
import com.example.myapplication.data.model.ChangePasswordRequest
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.LogoutResponse
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.RegisterResponse
import com.example.myapplication.data.model.ResetPasswordRequest
import com.example.myapplication.data.model.ResetPasswordResponse
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.model.common.requireData

open class LoginRepository(
    private val apiService: ApiService
) {

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     * @return LoginResponse si el servidor responde (incluso si es error 4xx/5xx Retrofit lanzará HttpException).
     */
    open suspend fun login(request: LoginRequest): LoginResponse {
        return apiService.login(request)
    }

    /**
     * Cierra la sesión activa en el backend.
     * El header se omite si el servidor no entregó un token al iniciar sesión.
     */
    open suspend fun logout(authorization: String?): LogoutResponse {
        return apiService.logout(authorization)
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    open suspend fun register(request: RegisterRequest): RegisterResponse {
        return apiService.register(request)
    }

    /**
     * Envía una solicitud de recuperación de contraseña.
     */
    open suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
        return apiService.forgotPassword(request)
    }

    /**
     * Restablece la contraseña utilizando el token.
     */
    open suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        return apiService.resetPassword(request)
    }

    open suspend fun getCurrentUser(): UserDto {
        return apiService.getCurrentUser().requireData()
    }

    open suspend fun changePassword(request: ChangePasswordRequest): ApiResponse<Any?> {
        return apiService.changePassword(request)
    }
}
