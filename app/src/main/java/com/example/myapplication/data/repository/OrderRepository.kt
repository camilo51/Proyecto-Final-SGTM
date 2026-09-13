package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.common.requireData

open class OrderRepository(
    private val apiService: ApiService
) {
    open suspend fun getOrders(): List<Order> {
        return apiService.getOrders().requireData()
    }

    open suspend fun getOrder(id: String): Order {
        return apiService.getOrder(id).requireData()
    }

    open suspend fun createOrder(order: Order): Order {
        return apiService.createOrder(order).requireData()
    }

    open suspend fun updateOrder(id: String, order: Order): Order {
        return apiService.updateOrder(id, order).requireData()
    }

    open suspend fun deleteOrder(id: String) {
        apiService.deleteOrder(id)
    }
}
