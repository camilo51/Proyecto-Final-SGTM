package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.common.requireData

open class ClientRepository(
    private val apiService: ApiService
) {

    open suspend fun getClients(): List<Client> {
        return apiService.getClients().requireData()
    }

    suspend fun getClient(id: String): Client {
        return apiService.getClient(id).requireData()
    }

    open suspend fun createClient(client: Client): Client {
        return apiService.createClient(client).requireData()
    }

    open suspend fun updateClient(
        id: String,
        client: Client
    ): Client {
        return apiService.updateClient(id, client).requireData()
    }

    open suspend fun deleteClient(id: String) {
        apiService.deleteClient(id)
    }
}
