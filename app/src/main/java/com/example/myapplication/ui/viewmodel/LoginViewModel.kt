package com.example.myapplication.ui.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.LoginErrorResponse
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.data.repository.LoginRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false,
    val user: UserDto? = null,
    val accessToken: String? = null,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: LoginRepository = LoginRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null
        )
    }

    /**
     * Limpia el mensaje de error actual
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun login() {
        val currentState = _uiState.value

        // Validaciones locales
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Por favor, completa todos los campos"
            )
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email.trim()).matches()) {
            _uiState.value = currentState.copy(
                errorMessage = "Ingresa un correo electrónico válido"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = repository.login(
                    LoginRequest(
                        email = currentState.email.trim(),
                        password = currentState.password
                    )
                )

                if (response.success && response.data != null) {
                    val user = response.data.user
                    val role = user.role?.lowercase() ?: ""
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        // Confiamos 100% en lo que venga de Aiven/API
                        isAdmin = role.contains("admin") || role == "1" || role == "administrador",
                        user = user,
                        accessToken = response.data.accessToken,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message.ifBlank { "Credenciales incorrectas" }
                    )
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    /**
     * Notifica el cierre de sesión al servidor y siempre limpia la sesión local.
     */
    fun logout(onComplete: () -> Unit) {
        val authorization = _uiState.value.accessToken?.let { "Bearer $it" }

        viewModelScope.launch {
            try {
                repository.logout(authorization)
            } catch (_: Exception) {
                // La sesión local debe cerrarse aunque la API no responda.
            } finally {
                _uiState.value = LoginUiState()
                onComplete()
            }
        }
    }

    /**
     * Lógica para recuperación de contraseña
     */
    fun forgotPassword(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Correo no válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.forgotPassword(ForgotPasswordRequest(email.trim()))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (response.success) "Correo enviado con éxito" else response.message
                )
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleException(e: Exception) {
        // Log interno para nosotros los desarrolladores
        Log.e("LoginViewModel", "Error detectado durante el proceso: ${e.message}", e)

        val emailContext = _uiState.value.email.ifBlank { "desconocido" }
        
        val message = when (e) {
            is SocketTimeoutException -> "El servidor tardó demasiado en responder. El usuario con el correo $emailContext no pudo ser verificado."
            is IOException -> "No hay conexión a internet. No se pudo establecer el estado de la cuenta para $emailContext."
            is HttpException -> {
                val errorReason = httpErrorMessage(e)
                "El usuario con el correo $emailContext no pudo acceder: $errorReason"
            }
            else -> "Ocurrió un error inesperado al intentar acceder con $emailContext. Por favor, contacta a soporte."
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    private fun httpErrorMessage(exception: HttpException): String {
        val errorBody = exception.response()?.errorBody()?.string()
        
        return try {
            if (!errorBody.isNullOrBlank()) {
                val errorResponse = Gson().fromJson(errorBody, LoginErrorResponse::class.java)
                errorResponse.message ?: errorResponse.errors?.firstOrNull()?.message ?: "Error del servidor"
            } else {
                "Error del servidor (${exception.code()})"
            }
        } catch (_: Exception) {
            "Error en la respuesta del servidor (${exception.code()})"
        }
    }
}
