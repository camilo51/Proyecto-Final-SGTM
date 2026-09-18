package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.repository.AuditRepository
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditUiState(
    val auditLogs: List<AuditLog> = emptyList(),
    val users: List<User> = emptyList(),
    val actions: List<String> = emptyList(),
    val tables: List<String> = emptyList(),
    val searchQuery: String = "",
    val fromDate: String? = null,
    val toDate: String? = null,
    val selectedUserId: String? = null,
    val selectedAction: String? = null,
    val selectedTable: String? = null,
    val totalRecords: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
) {
    val hasMore: Boolean
        get() = page < totalPages

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || fromDate != null || toDate != null ||
            selectedUserId != null || selectedAction != null || selectedTable != null
}

class AuditViewModel(
    private val auditRepository: AuditRepository = AuditRepository(),
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val testScope: CoroutineScope? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadInitialData() {
        loadFilterOptions()
        loadAuditLogs(resetPage = true)
    }

    fun refresh() = loadAuditLogs(resetPage = true)

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || !state.hasMore) return
        loadAuditLogs(page = state.page + 1, append = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFromDateChange(date: String?) {
        updateDates(fromDate = date, toDate = _uiState.value.toDate)
    }

    fun onToDateChange(date: String?) {
        updateDates(fromDate = _uiState.value.fromDate, toDate = date)
    }

    fun onUserFilterChange(userId: String?) {
        _uiState.update { it.copy(selectedUserId = userId, errorMessage = null) }
    }

    fun onActionFilterChange(action: String?) {
        _uiState.update { it.copy(selectedAction = action, errorMessage = null) }
    }

    fun onTableFilterChange(table: String?) {
        _uiState.update { it.copy(selectedTable = table, errorMessage = null) }
    }

    fun applyFilters(): Boolean {
        if (!hasValidDateRange(_uiState.value.fromDate, _uiState.value.toDate)) {
            _uiState.update { it.copy(errorMessage = "La fecha Desde no puede ser posterior a Hasta.") }
            return false
        }
        loadAuditLogs(resetPage = true)
        return true
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                fromDate = null,
                toDate = null,
                selectedUserId = null,
                selectedAction = null,
                selectedTable = null,
                errorMessage = null
            )
        }
        loadAuditLogs(resetPage = true)
    }

    private fun loadFilterOptions() {
        workScope.launch {
            val usersResult = runCatching { userRepository.getUsers() }
            val actionsResult = auditRepository.getActions()
            val tablesResult = auditRepository.getTables()

            _uiState.update { state ->
                state.copy(
                    users = usersResult.getOrDefault(emptyList()),
                    actions = (actionsResult as? NetworkResult.Success)?.data.orEmpty(),
                    tables = (tablesResult as? NetworkResult.Success)?.data.orEmpty()
                )
            }
        }
    }

    private fun loadAuditLogs(
        page: Int = 1,
        resetPage: Boolean = false,
        append: Boolean = false
    ) {
        val current = _uiState.value
        if (current.isLoading || current.isRefreshing || current.isLoadingMore) return

        val requestedPage = if (resetPage) 1 else page
        val filters = current.toFilters()
        workScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = !append && state.auditLogs.isEmpty(),
                    isRefreshing = !append && state.auditLogs.isNotEmpty(),
                    isLoadingMore = append,
                    errorMessage = null
                )
            }

            when (val result = auditRepository.getAuditLogs(filters, requestedPage)) {
                is NetworkResult.Success -> _uiState.update { state ->
                    val records = if (append) {
                        (state.auditLogs + result.data.items).distinctBy { log ->
                            log.id ?: "${log.createdAt}-${log.action}-${log.description}"
                        }
                    } else {
                        result.data.items
                    }
                    state.copy(
                        auditLogs = records,
                        totalRecords = result.data.pagination.total,
                        page = result.data.pagination.page,
                        totalPages = result.data.pagination.totalPages,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun updateDates(fromDate: String?, toDate: String?) {
        val error = if (hasValidDateRange(fromDate, toDate)) null else {
            "La fecha Desde no puede ser posterior a Hasta."
        }
        _uiState.update { it.copy(fromDate = fromDate, toDate = toDate, errorMessage = error) }
    }

    private fun AuditUiState.toFilters() = AuditFilters(
        search = searchQuery,
        fromDate = fromDate,
        toDate = toDate,
        userId = selectedUserId,
        action = selectedAction,
        tableName = selectedTable
    )

    private fun hasValidDateRange(fromDate: String?, toDate: String?): Boolean =
        fromDate == null || toDate == null || fromDate <= toDate
}
