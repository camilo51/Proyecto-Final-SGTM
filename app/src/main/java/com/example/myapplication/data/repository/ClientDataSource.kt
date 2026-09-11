package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Client

interface ClientDataSource : ClientLookup {
    suspend fun createClient(client: Client): Client

    suspend fun updateClient(id: String, client: Client): Client
}
