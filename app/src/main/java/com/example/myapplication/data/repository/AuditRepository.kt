package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedApiResponse
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.common.PaginationDto
import com.google.gson.Gson
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.Response

open class AuditRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val gson: Gson = Gson()
) {

    open suspend fun getAuditLogs(
        filters: AuditFilters,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE
    ): NetworkResult<PaginatedResult<AuditLog>> = executePaged {
        apiService.getAuditLogs(
            search = filters.search.trim().takeIf { it.isNotBlank() },
            dateFrom = filters.fromDate,
            dateTo = filters.toDate,
            userId = filters.userId,
            action = filters.action,
            tableName = filters.tableName,
            page = page.coerceAtLeast(1),
            limit = limit.coerceIn(1, MAX_PAGE_SIZE)
        )
    }

    open suspend fun getActions(): NetworkResult<List<String>> = execute {
        apiService.getAuditActions()
    }

    open suspend fun getTables(): NetworkResult<List<String>> = execute {
        apiService.getAuditTables()
    }

    private suspend fun <T> execute(
        call: suspend () -> Response<ApiResponse<T>>
    ): NetworkResult<T> {
        return try {
            val response = call()
            if (!response.isSuccessful) return response.toErrorResult()

            val body = response.body()
                ?: return NetworkResult.Error("El servidor no devolvió datos.", response.code())
            if (!body.success) return NetworkResult.Error(body.message.ifBlank { "No se pudo cargar la auditoría." }, response.code(), body.errors)
            val data = body.data
                ?: return NetworkResult.Error(body.message.ifBlank { "El servidor no devolvió datos." }, response.code())

            NetworkResult.Success(data, body.message)
        } catch (exception: SocketTimeoutException) {
            NetworkResult.Error("El servidor tardó demasiado en responder.")
        } catch (exception: IOException) {
            NetworkResult.Error("No hay conexión con el servidor.")
        } catch (exception: Exception) {
            NetworkResult.Error(exception.message ?: "No se pudo cargar la auditoría.")
        }
    }

    private suspend fun executePaged(
        call: suspend () -> Response<PaginatedApiResponse<AuditLog>>
    ): NetworkResult<PaginatedResult<AuditLog>> {
        return try {
            val response = call()
            if (!response.isSuccessful) return response.toErrorResult()

            val body = response.body()
                ?: return NetworkResult.Error("El servidor no devolvió datos.", response.code())
            if (!body.success) return NetworkResult.Error(body.message.ifBlank { "No se pudo cargar la auditoría." }, response.code(), body.errors)

            NetworkResult.Success(
                PaginatedResult(
                    items = body.data,
                    pagination = body.pagination ?: PaginationDto(limit = body.data.size)
                ),
                body.message
            )
        } catch (exception: SocketTimeoutException) {
            NetworkResult.Error("El servidor tardó demasiado en responder.")
        } catch (exception: IOException) {
            NetworkResult.Error("No hay conexión con el servidor.")
        } catch (exception: Exception) {
            NetworkResult.Error(exception.message ?: "No se pudo cargar la auditoría.")
        }
    }

    private fun <T> Response<T>.toErrorResult(): NetworkResult.Error {
        val backendMessage = runCatching {
            errorBody()?.charStream()?.use { reader ->
                gson.fromJson(reader, ApiResponse::class.java)?.message
            }
        }.getOrNull()

        val message = when (code()) {
            401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para consultar la auditoría."
            404 -> "No se encontraron registros de auditoría."
            else -> backendMessage?.takeIf { it.isNotBlank() } ?: "No se pudo cargar la auditoría."
        }
        return NetworkResult.Error(message, code())
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}
