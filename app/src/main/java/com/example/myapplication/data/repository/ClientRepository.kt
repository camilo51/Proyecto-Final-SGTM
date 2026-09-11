package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiException
import com.example.myapplication.data.api.ApiResponse
import com.example.myapplication.data.model.Client

class ClientRepository(
    private val apiService: ApiService
) : ClientDataSource {

    override suspend fun getClients(): List<Client> {
        return apiService.getClients().requireData()
    }

    suspend fun getClient(id: String): Client {
        return apiService.getClient(id).requireData()
    }

    override suspend fun createClient(client: Client): Client {
        return apiService.createClient(client).requireData()
    }

    override suspend fun updateClient(
        id: String,
        client: Client
    ): Client {
        return apiService.updateClient(id, client).requireData()
    }

    suspend fun deleteClient(id: String) {
        apiService.deleteClient(id)
    }

    private fun <T> ApiResponse<T>.requireData(): T {
        if (success && data != null) return data

        val errorMessage = message
            .ifBlank { errors?.firstOrNull()?.message.orEmpty() }
            .ifBlank { "La API no devolvió datos válidos" }
        throw ApiException(message = errorMessage)
    }
}
