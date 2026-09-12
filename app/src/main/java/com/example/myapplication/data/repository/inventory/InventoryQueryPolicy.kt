package com.example.myapplication.data.repository.inventory

import com.example.myapplication.data.model.inventory.InventoryListQuery

object InventoryQueryPolicy {
    fun normalize(query: InventoryListQuery): InventoryListQuery = query.copy(
        search = query.search.trim(),
        category = query.category?.trim()?.takeIf { it.isNotEmpty() },
        brand = query.brand?.trim()?.takeIf { it.isNotEmpty() },
        status = query.status?.trim()?.takeIf { it.isNotEmpty() },
        page = query.page.coerceAtLeast(1),
        limit = query.limit.coerceIn(1, 100)
    )
}
