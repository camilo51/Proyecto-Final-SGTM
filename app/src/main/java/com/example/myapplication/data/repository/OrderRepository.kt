package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Order

class OrderRepository(
    private val apiService: ApiService
) {
    suspend fun getOrders(): List<Order> {
        return apiService.getOrders()
    }

    suspend fun getOrder(id: String): Order {
        return apiService.getOrder(id)
    }

    suspend fun createOrder(order: Order): Order {
        return apiService.createOrder(order)
    }

    suspend fun updateOrder(id: String, order: Order): Order {
        return apiService.updateOrder(id, order)
    }

    suspend fun deleteOrder(id: String) {
        apiService.deleteOrder(id)
    }
}
