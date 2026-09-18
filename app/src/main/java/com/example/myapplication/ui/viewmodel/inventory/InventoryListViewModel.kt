package com.example.myapplication.ui.viewmodel.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.model.inventory.InventoryAlertSummary
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.model.inventory.InventorySort
import com.example.myapplication.data.repository.inventory.InventoryRefreshBus
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryListUiState(
    val items: List<InventoryDto> = emptyList(),
    val query: InventoryListQuery = InventoryListQuery(),
    val categories: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val alerts: InventoryAlertSummary = InventoryAlertSummary(),
    val pagination: PaginationDto = PaginationDto(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class InventoryListViewModel(
    private val repository: InventoryRepository = InventoryRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOptions()
        refresh()
        viewModelScope.launch {
            InventoryRefreshBus.changes.collect { refresh() }
        }
    }

    fun onSearchChange(search: String) {
        _uiState.value = _uiState.value.copy(query = _uiState.value.query.copy(search = search))
    }

    fun search() = load(page = 1)

    fun setCategory(category: String?) {
        _uiState.value = _uiState.value.copy(query = _uiState.value.query.copy(category = category))
        load(page = 1)
    }

    fun setBrand(brand: String?) {
        _uiState.value = _uiState.value.copy(query = _uiState.value.query.copy(brand = brand))
        load(page = 1)
    }

    fun setStatus(status: String?) {
        _uiState.value = _uiState.value.copy(query = _uiState.value.query.copy(status = status))
        load(page = 1)
    }

    fun setSort(sort: InventorySort) {
        _uiState.value = _uiState.value.copy(query = _uiState.value.query.copy(sort = sort))
        load(page = 1)
    }

    fun goToPage(page: Int) = load(page)

    fun refresh() {
        load(page = _uiState.value.query.page, refreshing = true)
        loadAlerts()
    }

    private fun load(page: Int, refreshing: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
        val query = _uiState.value.query.copy(page = page.coerceAtLeast(1))
        _uiState.value = _uiState.value.copy(
            query = query,
            isLoading = !refreshing,
            isRefreshing = refreshing,
            errorMessage = null
        )
        viewModelScope.launch {
            when (val result = repository.list(query)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    items = result.data.items,
                    pagination = result.data.pagination,
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

    private fun loadOptions() {
        viewModelScope.launch {
            when (val categories = repository.getCategories()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(categories = categories.data)
                else -> Unit
            }
            when (val brands = repository.getBrands()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(brands = brands.data)
                else -> Unit
            }
        }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            when (val alerts = repository.getAlerts()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(alerts = alerts.data)
                else -> Unit
            }
        }
    }
}
