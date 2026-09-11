package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client

class ClientRepository(
    private val apiService: ApiService
) : ClientLookup {

    override suspend fun getClients(): List<Client> {
        return apiService.getClients()
    }

    suspend fun getClient(id: String): Client {
        return apiService.getClient(id)
    }

    suspend fun createClient(client: Client): Client {
        return apiService.createClient(client)
    }

    suspend fun updateClient(
        id: String,
        client: Client
    ): Client {
        return apiService.updateClient(id, client)
    }

    suspend fun deleteClient(id: String) {
        apiService.deleteClient(id)
    }
}
