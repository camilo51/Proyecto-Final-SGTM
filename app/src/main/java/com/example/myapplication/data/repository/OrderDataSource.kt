package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order

interface OrderDataSource {
    suspend fun getOrders(): List<Order>
    suspend fun getOrder(id: String): Order
    suspend fun createOrder(order: Order): Order
    suspend fun updateOrder(id: String, order: Order): Order
    suspend fun deleteOrder(id: String)
}

interface ClientLookup {
    suspend fun getClients(): List<Client>
}

interface MotorcycleLookup {
    suspend fun getMotorcycles(): List<Motorcycle>
}
