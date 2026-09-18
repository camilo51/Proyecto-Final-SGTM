package com.example.myapplication.data.repository.inventory

import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.inventory.CreateInventoryRequest
import com.example.myapplication.data.model.inventory.DeleteInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryAlertSummary
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.model.inventory.InventoryMovementsPage
import com.example.myapplication.data.model.inventory.InventoryPartReference
import com.example.myapplication.data.model.inventory.StockAdjustmentRequest
import com.example.myapplication.data.model.inventory.StockEntryRequest
import com.example.myapplication.data.model.inventory.StockMovementResultDto
import com.example.myapplication.data.model.inventory.StockOutputRequest
import com.example.myapplication.data.model.inventory.UpdateInventoryRequest

interface InventorySelector {
    suspend fun searchAvailableParts(
        search: String,
        page: Int,
        limit: Int
    ): PaginatedResult<InventoryPartReference>
}

interface InventoryRepository : InventorySelector {
    suspend fun list(query: InventoryListQuery): NetworkResult<PaginatedResult<InventoryDto>>
    suspend fun getCategories(): NetworkResult<List<String>>
    suspend fun getBrands(): NetworkResult<List<String>>
    suspend fun getAlerts(status: String? = null): NetworkResult<InventoryAlertSummary>
    suspend fun getDetail(id: Long): NetworkResult<InventoryDto>
    suspend fun getMovements(id: Long, page: Int, limit: Int): NetworkResult<InventoryMovementsPage>
    suspend fun create(request: CreateInventoryRequest): NetworkResult<InventoryDto>
    suspend fun update(id: Long, request: UpdateInventoryRequest): NetworkResult<InventoryDto>
    suspend fun registerEntry(id: Long, request: StockEntryRequest): NetworkResult<StockMovementResultDto>
    suspend fun registerOutput(id: Long, request: StockOutputRequest): NetworkResult<StockMovementResultDto>
    suspend fun registerAdjustment(id: Long, request: StockAdjustmentRequest): NetworkResult<StockMovementResultDto>
    suspend fun delete(id: Long, request: DeleteInventoryRequest): NetworkResult<Unit>
}
