package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Inventory

open class InventoryRepository(
    private val apiService: ApiService
) {

    open suspend fun getInventory(): List<Inventory> {
        return apiService.getInventory()
    }

    open suspend fun getInventoryItem(id: String): Inventory {
        return apiService.getInventoryItem(id)
    }

    open suspend fun createInventoryItem(inventory: Inventory): Inventory {
        return apiService.createInventoryItem(inventory)
    }

    open suspend fun updateInventoryItem(id: String, inventory: Inventory): Inventory {
        return apiService.updateInventoryItem(id, inventory)
    }

    open suspend fun deleteInventoryItem(id: String) {
        apiService.deleteInventoryItem(id)
    }
}
