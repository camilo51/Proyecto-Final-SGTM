package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ChangePasswordRequest
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.data.model.common.ApiException
import com.example.myapplication.data.repository.LoginRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.data.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class ProfileUiState(
    val user: UserDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val loginRepository: LoginRepository = LoginRepository(RetrofitClient.apiService),
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val testScope: CoroutineScope? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadProfile(fallbackUser: UserDto? = null) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            user = _uiState.value.user ?: fallbackUser,
            isLoading = true,
            errorMessage = null,
            successMessage = null
        )
        workScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    user = loginRepository.getCurrentUser(),
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = friendlyError(exception, "No se pudo cargar el perfil.")
                )
            }
        }
    }

    fun updateProfile(name: String, email: String, onUserUpdated: (UserDto) -> Unit = {}) {
        val currentUser = _uiState.value.user
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        when {
            currentUser?.id.isNullOrBlank() -> {
                setError("No se encontró el identificador del usuario.")
                return
            }
            cleanName.isBlank() -> {
                setError("El nombre de usuario es obligatorio.")
                return
            }
            !EMAIL_PATTERN.matches(cleanEmail) -> {
                setError("Ingresa un correo electrónico válido.")
                return
            }
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
        workScope.launch {
            try {
                val updated = userRepository.updateUser(
                    id = currentUser?.id.orEmpty(),
                    user = User(
                        id = currentUser?.id,
                        name = cleanName,
                        email = cleanEmail,
                        avatar = currentUser?.avatar
                    )
                )
                val updatedDto = UserDto(
                    id = updated.id ?: currentUser?.id,
                    name = updated.name,
                    email = updated.email,
                    avatar = updated.avatar,
                    role = currentUser?.role
                )
                _uiState.value = _uiState.value.copy(
                    user = updatedDto,
                    isSaving = false,
                    errorMessage = null,
                    successMessage = "Información actualizada correctamente"
                )
                onUserUpdated(updatedDto)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = friendlyError(exception, "No se pudo actualizar la información.")
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmation: String) {
        when {
            currentPassword.isBlank() -> {
                setError("Ingresa tu contraseña actual.")
                return
            }
            newPassword.length < MIN_PASSWORD_LENGTH -> {
                setError("La nueva contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres.")
                return
            }
            newPassword != confirmation -> {
                setError("La confirmación de contraseña no coincide.")
                return
            }
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
        workScope.launch {
            try {
                val response = loginRepository.changePassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )
                if (!response.success) {
                    throw ApiException(message = response.message.ifBlank { "No se pudo cambiar la contraseña." })
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = null,
                    successMessage = "Contraseña actualizada correctamente"
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = friendlyError(exception, "No se pudo cambiar la contraseña.")
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, successMessage = null)
    }

    private fun friendlyError(exception: Exception, fallback: String): String {
        return when (exception) {
            is SocketTimeoutException -> "El servidor tardó demasiado en responder."
            is IOException -> "No hay conexión con el servidor."
            is HttpException -> when (exception.code()) {
                401 -> "Tu sesión expiró. Inicia sesión nuevamente."
                403 -> "No tienes permisos para realizar esta acción."
                404 -> "No se pudo cargar el perfil."
                409 -> conflictMessage(exception) ?: "El cambio entra en conflicto con otro usuario."
                400 -> "La contraseña actual no es correcta."
                else -> fallback
            }
            is ApiException -> exception.message.ifBlank { fallback }
            else -> fallback
        }
    }

    private fun conflictMessage(exception: HttpException): String? {
        val body = exception.response()?.errorBody()?.string() ?: return null
        return runCatching {
            val response = Gson().fromJson(body, com.example.myapplication.data.model.LoginErrorResponse::class.java)
            response.message ?: response.errors?.firstOrNull()?.message
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
