package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.myapplication.data.model.User

data class UserUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val visibleUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val pageSize: Int = USERS_PAGE_SIZE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val operationMessage: String? = null,
    val creationVersion: Int = 0,
    val updateVersion: Int = 0,
    val deleteVersion: Int = 0
) {
    val totalPages: Int
        get() = if (filteredUsers.isEmpty()) {
            0
        } else {
            (filteredUsers.size + pageSize - 1) / pageSize
        }
}

class UserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val localUsers = mutableListOf(
        User("1", "José", "jose@example.com", null),
        User("2", "Danilo", "danilo@example.com", null),
        User("3", "Sgtm Admin", "admin@enginesjds.com", null)
    )

    init {
        loadUsers()
    }

    fun loadUsers() {
        _uiState.update {
            it.copy(
                users = localUsers.toList(),
                isLoading = false,
                errorMessage = null
            ).withFilters()
        }
    }

    fun refreshUsers() {
        _uiState.update { it.copy(isRefreshing = true) }
        _uiState.update {
            it.copy(
                users = localUsers.toList(),
                isRefreshing = false,
                errorMessage = null
            ).withFilters()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    fun onPageChange(page: Int) {
        _uiState.update { state ->
            val lastPage = state.totalPages.coerceAtLeast(1)
            val nextPage = page.coerceIn(1, lastPage)
            state.copy(
                currentPage = nextPage,
                visibleUsers = state.pageItems(nextPage)
            )
        }
    }

    fun createUser(
        name: String,
        email: String,
        role: String? = null,
        avatar: String? = null
    ) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        val newId = (localUsers.mapNotNull { it.id?.toIntOrNull() }.maxOrNull() ?: 0) + 1
        val newUser = User(
            id = newId.toString(),
            name = name.trim(),
            email = normalizedEmail,
            role = role?.trim()?.takeIf(String::isNotBlank),
            avatar = avatar?.trim()?.takeIf(String::isNotBlank)
        )
        localUsers.add(newUser)

        _uiState.update {
            it.copy(
                users = localUsers.toList(),
                isSaving = false,
                operationMessage = "Usuario creado correctamente",
                creationVersion = it.creationVersion + 1
            ).withFilters()
        }
    }

    fun updateUser(user: User) {
        val id = user.id?.trim()?.takeIf(String::isNotBlank)
        if (id == null) {
            _uiState.update {
                it.copy(operationMessage = "No se puede editar un usuario sin identificador")
            }
            return
        }

        val normalizedEmail = user.email.trim()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        val index = localUsers.indexOfFirst { it.id == id }
        if (index != -1) {
            localUsers[index] = user.copy(
                name = user.name.trim(),
                email = normalizedEmail,
                role = user.role?.trim()?.takeIf(String::isNotBlank),
                avatar = user.avatar?.trim()?.takeIf(String::isNotBlank)
            )
        }

        _uiState.update {
            it.copy(
                users = localUsers.toList(),
                isSaving = false,
                operationMessage = "Usuario actualizado correctamente",
                updateVersion = it.updateVersion + 1
            ).withFilters()
        }
    }

    fun deleteUser(id: String) {
        _uiState.update { it.copy(isSaving = true) }
        localUsers.removeAll { it.id == id }
        _uiState.update {
            it.copy(
                users = localUsers.toList(),
                isSaving = false,
                operationMessage = "Usuario eliminado correctamente",
                deleteVersion = it.deleteVersion + 1
            ).withFilters()
        }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }

    private fun UserUiState.withFilters(): UserUiState {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.ROOT)
        val filtered = if (normalizedQuery.isBlank()) {
            users
        } else {
            users.filter { user ->
                listOf(
                    user.name,
                    user.email,
                    user.id.orEmpty()
                )
                    .any { value -> value.lowercase(Locale.ROOT).contains(normalizedQuery) }
            }
        }

        return copy(
            filteredUsers = filtered,
            currentPage = 1,
            visibleUsers = filtered.take(pageSize)
        )
    }

    private fun UserUiState.pageItems(page: Int): List<User> {
        val startIndex = (page - 1) * pageSize
        return filteredUsers.drop(startIndex).take(pageSize)
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

private const val USERS_PAGE_SIZE = 10
