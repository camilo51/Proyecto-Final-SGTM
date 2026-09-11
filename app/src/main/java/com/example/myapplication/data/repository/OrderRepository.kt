package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Order

class OrderRepository(
    private val apiService: ApiService
) : OrderDataSource {
    override suspend fun getOrders(): List<Order> {
        return apiService.getOrders()
    }

    override suspend fun getOrder(id: String): Order {
        return apiService.getOrder(id)
    }

    override suspend fun createOrder(order: Order): Order {
        return apiService.createOrder(order)
    }

    override suspend fun updateOrder(id: String, order: Order): Order {
        return apiService.updateOrder(id, order)
    }

    override suspend fun deleteOrder(id: String) {
        apiService.deleteOrder(id)
    }
}
