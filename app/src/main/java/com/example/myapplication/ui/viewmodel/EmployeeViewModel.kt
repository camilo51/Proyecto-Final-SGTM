package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.myapplication.data.model.Employee

data class EmployeeUiState(
    val employees: List<Employee> = emptyList(),
    val filteredEmployees: List<Employee> = emptyList(),
    val visibleEmployees: List<Employee> = emptyList(),
    val searchQuery: String = "",
    val selectedRole: String = "Todas",
    val currentPage: Int = 1,
    val pageSize: Int = EMPLOYEES_PAGE_SIZE,
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
        get() = if (filteredEmployees.isEmpty()) {
            0
        } else {
            (filteredEmployees.size + pageSize - 1) / pageSize
        }
}

class EmployeeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    private val localEmployees = mutableListOf(
        Employee("1", "José Aparicio", "jogeapa@hotmail.com", "3008909195", "Mecánica general"),
        Employee("2", "Isaac Vento", "isaac@example.com", "3147929430", "Mecánica general"),
        Employee("3", "Chunt Vento", "chunt@example.com", "3045624769", "Mecánica básica")
    )

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        _uiState.update { 
            it.copy(
                employees = localEmployees.toList(),
                isLoading = false,
                errorMessage = null
            ).withFilters()
        }
    }

    fun refreshEmployees() {
        _uiState.update { it.copy(isRefreshing = true) }
        _uiState.update {
            it.copy(
                employees = localEmployees.toList(),
                isRefreshing = false,
                errorMessage = null
            ).withFilters()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun onRoleFilterChange(role: String) {
        _uiState.update { it.copy(selectedRole = role).withFilters() }
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
                visibleEmployees = state.pageItems(nextPage)
            )
        }
    }

    fun createEmployee(
        name: String,
        email: String,
        phone: String,
        role: String
    ) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        val newId = (localEmployees.mapNotNull { it.id?.toIntOrNull() }.maxOrNull() ?: 0) + 1
        val newEmployee = Employee(
            id = newId.toString(),
            name = name.trim(),
            email = normalizedEmail,
            phone = phone.trim(),
            role = role.trim()
        )
        localEmployees.add(newEmployee)

        _uiState.update {
            it.copy(
                employees = localEmployees.toList(),
                isSaving = false,
                operationMessage = "Empleado creado correctamente",
                creationVersion = it.creationVersion + 1
            ).withFilters()
        }
    }

    fun updateEmployee(employee: Employee) {
        val id = employee.id?.trim()?.takeIf(String::isNotBlank)
        if (id == null) {
            _uiState.update {
                it.copy(operationMessage = "No se puede editar un empleado sin identificador")
            }
            return
        }

        val normalizedEmail = employee.email?.trim().orEmpty()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        val index = localEmployees.indexOfFirst { it.id == id }
        if (index != -1) {
            localEmployees[index] = employee.copy(
                name = employee.name.trim(),
                email = normalizedEmail,
                phone = employee.phone.trim(),
                role = employee.role?.trim()
            )
        }

        _uiState.update {
            it.copy(
                employees = localEmployees.toList(),
                isSaving = false,
                operationMessage = "Empleado actualizado correctamente",
                updateVersion = it.updateVersion + 1
            ).withFilters()
        }
    }

    fun deleteEmployee(id: String) {
        _uiState.update { it.copy(isSaving = true) }
        localEmployees.removeAll { it.id == id }
        _uiState.update {
            it.copy(
                employees = localEmployees.toList(),
                isSaving = false,
                operationMessage = "Empleado eliminado correctamente",
                deleteVersion = it.deleteVersion + 1
            ).withFilters()
        }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }

    private fun EmployeeUiState.withFilters(): EmployeeUiState {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.ROOT)
        var filtered = if (normalizedQuery.isBlank()) {
            employees
        } else {
            employees.filter { employee ->
                listOf(
                    employee.name,
                    employee.email.orEmpty(),
                    employee.phone,
                    employee.role.orEmpty(),
                    employee.id.orEmpty()
                )
                    .any { value -> value.lowercase(Locale.ROOT).contains(normalizedQuery) }
            }
        }

        if (selectedRole != "Todas") {
            filtered = filtered.filter { it.role == selectedRole }
        }

        return copy(
            filteredEmployees = filtered,
            currentPage = 1,
            visibleEmployees = filtered.take(pageSize)
        )
    }

    private fun EmployeeUiState.pageItems(page: Int): List<Employee> {
        val startIndex = (page - 1) * pageSize
        return filteredEmployees.drop(startIndex).take(pageSize)
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

private const val EMPLOYEES_PAGE_SIZE = 10
