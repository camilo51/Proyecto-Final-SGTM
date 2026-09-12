package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Motorcycle

open class MotorcycleRepository(
    private val apiService: ApiService
) {
    open suspend fun getMotorcycles(): List<Motorcycle> {
        return apiService.getMotorcycles()
    }

    suspend fun getMotorcycle(id: String): Motorcycle {
        return apiService.getMotorcycle(id)
    }

    suspend fun createMotorcycle(motorcycle: Motorcycle): Motorcycle {
        return apiService.createMotorcycle(motorcycle)
    }

    suspend fun updateMotorcycle(id: String, motorcycle: Motorcycle): Motorcycle {
        return apiService.updateMotorcycle(id, motorcycle)
    }

    suspend fun deleteMotorcycle(id: String) {
        apiService.deleteMotorcycle(id)
    }
}
