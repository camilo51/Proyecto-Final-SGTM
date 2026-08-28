package com.example.myapplication.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
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
    val user: UserDto? = null,
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

    fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Ingresa tu correo y contraseña"
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
                isLoggedIn = false,
                user = null,
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = response.data.user,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message.ifBlank {
                            "No se pudo iniciar sesión"
                        }
                    )
                }
            } catch (_: SocketTimeoutException) {
                showError("El servidor tardó demasiado en responder")
            } catch (_: IOException) {
                showError("No hay conexión con el servidor")
            } catch (exception: HttpException) {
                showError(httpErrorMessage(exception))
            } catch (_: Exception) {
                showError("Ocurrió un error al iniciar sesión")
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoggedIn = false,
            user = null,
            errorMessage = message
        )
    }

    private fun httpErrorMessage(exception: HttpException): String {
        val errorBody = exception.response()?.errorBody()?.string()

        if (!errorBody.isNullOrBlank()) {
            runCatching {
                Gson().fromJson(errorBody, LoginErrorResponse::class.java)
            }.getOrNull()?.let { error ->
                error.errors
                    ?.firstOrNull { !it.message.isNullOrBlank() }
                    ?.message
                    ?.let { return it }

                error.message
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return it }
            }
        }

        return when (exception.code()) {
            401 -> "Correo o contraseña incorrectos"
            400 -> "Revisa los datos ingresados"
            else -> "Error del servidor (${exception.code()})"
        }
    }
}
