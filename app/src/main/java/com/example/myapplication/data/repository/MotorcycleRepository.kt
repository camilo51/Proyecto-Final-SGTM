package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Motorcycle

open class MotorcycleRepository(
    private val apiService: ApiService
) {

    open suspend fun getMotorcycles(): List<Motorcycle> {
        return apiService.getMotorcycles()
    }

    open suspend fun getMotorcycle(id: String): Motorcycle {
        return apiService.getMotorcycle(id)
    }

    open suspend fun createMotorcycle(motorcycle: Motorcycle): Motorcycle {
        return apiService.createMotorcycle(motorcycle)
    }

    open suspend fun updateMotorcycle(id: String, motorcycle: Motorcycle): Motorcycle {
        return apiService.updateMotorcycle(id, motorcycle)
    }

    open suspend fun deleteMotorcycle(id: String) {
        apiService.deleteMotorcycle(id)
    }
}
