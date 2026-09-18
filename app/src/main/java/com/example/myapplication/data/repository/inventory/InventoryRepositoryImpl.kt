package com.example.myapplication.data.repository.inventory

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedApiResponse
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.model.inventory.CreateInventoryRequest
import com.example.myapplication.data.model.inventory.DeleteInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryAlertDto
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
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

class InventoryRepositoryImpl(
    private val service: ApiService = RetrofitClient.apiService,
    private val gson: Gson = Gson()
) : InventoryRepository {

    override suspend fun list(query: InventoryListQuery): NetworkResult<PaginatedResult<InventoryDto>> {
        val normalizedQuery = InventoryQueryPolicy.normalize(query)
        return when (val result = executePaged {
            service.getInventoryPage(
                search = normalizedQuery.search.takeIf { it.isNotBlank() },
                category = normalizedQuery.category,
                brand = normalizedQuery.brand,
                status = normalizedQuery.status,
                sort = normalizedQuery.sort.apiValue,
                page = normalizedQuery.page,
                limit = normalizedQuery.limit
            )
        }) {
            is NetworkResult.Success -> result
            is NetworkResult.Error -> result
        }
    }

    override suspend fun getCategories(): NetworkResult<List<String>> = execute {
        service.getInventoryCategories()
    }

    override suspend fun getBrands(): NetworkResult<List<String>> = execute {
        service.getInventoryBrands()
    }

    override suspend fun getAlerts(status: String?): NetworkResult<InventoryAlertSummary> {
        return when (val result = execute<List<InventoryAlertDto>> { service.getInventoryAlerts(status) }) {
            is NetworkResult.Success -> {
                val alerts = result.data
                NetworkResult.Success(InventoryAlertRules.summarize(alerts), result.message)
            }
            is NetworkResult.Error -> result
        }
    }

    override suspend fun getDetail(id: Long): NetworkResult<InventoryDto> = execute {
        service.getInventoryDetail(id)
    }

    override suspend fun getMovements(
        id: Long,
        page: Int,
        limit: Int
    ): NetworkResult<InventoryMovementsPage> {
        return when (val result = executePaged {
            service.getInventoryMovements(id, page.coerceAtLeast(1), limit.coerceIn(1, 100))
        }) {
            is NetworkResult.Success -> NetworkResult.Success(
                InventoryMovementsPage(result.data.items, result.data.pagination),
                result.message
            )
            is NetworkResult.Error -> result
        }
    }

    override suspend fun create(request: CreateInventoryRequest): NetworkResult<InventoryDto> =
        executeMutation { service.createInventory(request) }

    override suspend fun update(id: Long, request: UpdateInventoryRequest): NetworkResult<InventoryDto> =
        executeMutation { service.updateInventory(id, request) }

    override suspend fun registerEntry(
        id: Long,
        request: StockEntryRequest
    ): NetworkResult<StockMovementResultDto> = executeMutation { service.registerInventoryEntry(id, request) }

    override suspend fun registerOutput(
        id: Long,
        request: StockOutputRequest
    ): NetworkResult<StockMovementResultDto> = executeMutation { service.registerInventoryOutput(id, request) }

    override suspend fun registerAdjustment(
        id: Long,
        request: StockAdjustmentRequest
    ): NetworkResult<StockMovementResultDto> = executeMutation { service.registerInventoryAdjustment(id, request) }

    override suspend fun delete(id: Long, request: DeleteInventoryRequest): NetworkResult<Unit> {
        return try {
            val response = service.deleteInventory(id, request)
            if (InventoryHttpErrorPolicy.isSuccessfulDelete(response.code())) {
                InventoryRefreshBus.notifyChanged()
                NetworkResult.Success(Unit)
            } else {
                response.toErrorResult()
            }
        } catch (exception: IOException) {
            NetworkResult.Error("No fue posible conectar con el servidor.")
        } catch (exception: Exception) {
            NetworkResult.Error(exception.message ?: "No fue posible eliminar el repuesto.")
        }
    }

    override suspend fun searchAvailableParts(
        search: String,
        page: Int,
        limit: Int
    ): PaginatedResult<InventoryPartReference> {
        return when (val result = list(
            InventoryListQuery(
                search = search,
                status = "Disponible",
                page = page,
                limit = limit
            )
        )) {
            is NetworkResult.Success -> PaginatedResult(
                items = result.data.items.map { item ->
                    InventoryPartReference(
                        id = item.id,
                        code = item.code,
                        name = item.name,
                        brand = item.brand,
                        salePrice = item.salePrice,
                        quantity = item.quantity,
                        status = item.status
                    )
                },
                pagination = result.data.pagination
            )
            is NetworkResult.Error -> throw InventorySelectorException(result.message, result.code)
        }
    }

    private suspend fun <T> executeMutation(
        call: suspend () -> Response<ApiResponse<T>>
    ): NetworkResult<T> {
        val result = execute(call)
        if (result is NetworkResult.Success) {
            InventoryRefreshBus.notifyChanged()
        }
        return result
    }

    private suspend fun <T> execute(
        call: suspend () -> Response<ApiResponse<T>>
    ): NetworkResult<T> {
        return try {
            val response = call()
            if (!response.isSuccessful) return response.toErrorResult()

            val body = response.body()
                ?: return NetworkResult.Error("El servidor no devolvió datos.", response.code())
            if (!body.success) return NetworkResult.Error(body.message, response.code(), body.errors)
            val data = body.data
                ?: return NetworkResult.Error(body.message.ifBlank { "El servidor no devolvió datos." }, response.code())
            NetworkResult.Success(data, body.message)
        } catch (exception: IOException) {
            NetworkResult.Error("No fue posible conectar con el servidor.")
        } catch (exception: Exception) {
            NetworkResult.Error(exception.message ?: "Ocurrió un error de red.")
        }
    }

    private suspend fun <T> executePaged(
        call: suspend () -> Response<PaginatedApiResponse<T>>
    ): NetworkResult<PaginatedResult<T>> {
        return try {
            val response = call()
            if (!response.isSuccessful) return response.toErrorResult()

            val body = response.body()
                ?: return NetworkResult.Error("El servidor no devolvió datos.", response.code())
            if (!body.success) return NetworkResult.Error(body.message, response.code(), body.errors)
            NetworkResult.Success(
                PaginatedResult(
                    items = body.data,
                    pagination = body.pagination ?: PaginationDto(limit = body.data.size)
                ),
                body.message
            )
        } catch (exception: IOException) {
            NetworkResult.Error("No fue posible conectar con el servidor.")
        } catch (exception: Exception) {
            NetworkResult.Error(exception.message ?: "Ocurrió un error de red.")
        }
    }

    private fun <T> Response<T>.toErrorResult(): NetworkResult<Nothing> {
        val apiError = runCatching {
            gson.fromJson(errorBody()?.charStream(), ApiResponse::class.java)
        }.getOrNull()
        val message = InventoryHttpErrorPolicy.messageFor(code(), apiError?.message)
        return NetworkResult.Error(message, code())
    }
}

class InventorySelectorException(message: String, val statusCode: Int?) : Exception(message)
