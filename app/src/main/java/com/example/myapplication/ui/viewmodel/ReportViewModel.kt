package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Report
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.repository.ReportRepository
import com.example.myapplication.data.repository.inventory.InventoryRefreshBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val report: Report? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class ReportViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            InventoryRefreshBus.changes.collect { refresh() }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        val isInitialLoad = _uiState.value.report == null
        _uiState.value = _uiState.value.copy(
            isLoading = isInitialLoad,
            isRefreshing = !isInitialLoad,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = repository.getInventoryReport()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    report = result.data,
                    isLoading = false,
                    isRefreshing = false
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = result.message
                )
            }
        }
    }
}
