package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.ClientRequest
import com.example.myapplication.data.model.DeleteClientRequest
import com.example.myapplication.data.model.common.requireData

open class ClientRepository(
    private val apiService: ApiService
) {

    open suspend fun getClients(): List<Client> {
        return apiService.getClients().requireData()
    }

    open suspend fun getClient(id: String): Client {
        return apiService.getClient(id).requireData()
    }

    open suspend fun createClient(request: ClientRequest): Client {
        return apiService.createClient(request).requireData()
    }

    open suspend fun updateClient(
        id: String,
        request: ClientRequest
    ): Client {
        return apiService.updateClient(id, request).requireData()
    }

    open suspend fun deleteClient(id: String, reason: String) {
        apiService.deleteClient(id, DeleteClientRequest(reason))
    }
}
