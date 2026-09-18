package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.common.requireData
import com.google.gson.JsonObject

open class OrderRepository(
    private val apiService: ApiService
) {
    open suspend fun getOrders(): List<Order> {
        return apiService.getOrders().requireData().items
    }

    open suspend fun getOrder(id: String): Order {
        return apiService.getOrder(id).requireData()
    }

    open suspend fun createOrder(order: Order): Order {
        return apiService.createOrder(order.toCreateJson()).requireData()
    }

    open suspend fun changeOrderStatus(id: String, status: String): Order {
        val body = JsonObject().apply { addProperty("status", status) }
        return apiService.changeOrderStatus(id, body).requireData()
    }

    open suspend fun updateOrder(id: String, order: Order): Order {
        return apiService.updateOrder(id, order).requireData()
    }

    open suspend fun deleteOrder(id: String) {
        apiService.deleteOrder(id)
    }

    private fun Order.toCreateJson(): JsonObject = JsonObject().apply {
        addNumericId("client_id", clientId)
        addNumericId("motorcycle_id", motorcycleId)
        addProperty("problem_description", description.trim())
        assignedEmployeeId?.trim()?.takeIf(String::isNotEmpty)?.let {
            addNumericId("assigned_employee_id", it)
        }
        appointmentId?.trim()?.takeIf(String::isNotEmpty)?.let {
            addNumericId("appointment_id", it)
        }
        laborCost?.let { addProperty("labor_cost", it) }
        discount?.let { addProperty("discount", it) }
    }

    private fun JsonObject.addNumericId(name: String, value: String) {
        value.toLongOrNull()?.let { addProperty(name, it) } ?: addProperty(name, value)
    }
}
