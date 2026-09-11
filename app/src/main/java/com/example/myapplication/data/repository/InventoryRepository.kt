package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Inventory

class InventoryRepository(
    private val apiService: ApiService
) {
    suspend fun getInventory(): List<Inventory> {
        return apiService.getInventory()
    }

    suspend fun getInventoryItem(id: String): Inventory {
        return apiService.getInventoryItem(id)
    }

    suspend fun createInventoryItem(inventory: Inventory): Inventory {
        return apiService.createInventoryItem(inventory)
    }

    suspend fun updateInventoryItem(id: String, inventory: Inventory): Inventory {
        return apiService.updateInventoryItem(id, inventory)
    }

    suspend fun deleteInventoryItem(id: String) {
        apiService.deleteInventoryItem(id)
    }
}
