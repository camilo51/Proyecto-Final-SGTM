package com.example.myapplication.data.model

import com.example.myapplication.data.model.inventory.InventoryDto
import java.math.BigDecimal

data class RevenuePeriod(
    val sales: BigDecimal = BigDecimal.ZERO,
    val workOrders: BigDecimal = BigDecimal.ZERO
) {
    val total: BigDecimal
        get() = sales + workOrders
}

/** Resumen calculado con los datos reales de inventario, facturas y órdenes. */
data class Report(
    val dailyRevenue: RevenuePeriod = RevenuePeriod(),
    val fortnightRevenue: RevenuePeriod = RevenuePeriod(),
    val monthlyRevenue: RevenuePeriod = RevenuePeriod(),
    val financialError: String? = null,
    val totalReferences: Int = 0,
    val totalUnits: Int = 0,
    val inventoryCost: BigDecimal = BigDecimal.ZERO,
    val potentialSales: BigDecimal = BigDecimal.ZERO,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val lowStockParts: List<InventoryDto> = emptyList(),
    val outOfStockParts: List<InventoryDto> = emptyList(),
    val topSellingParts: List<InventoryDto> = emptyList(),
)
