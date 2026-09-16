package com.example.myapplication.data.repository.inventory

import com.example.myapplication.data.model.inventory.InventoryAlertDto
import com.example.myapplication.data.model.inventory.InventoryAlertSummary

object InventoryAlertRules {
    fun summarize(alerts: List<InventoryAlertDto>): InventoryAlertSummary = InventoryAlertSummary(
        lowStockCount = alerts.count { it.status.equals("Stock bajo", ignoreCase = true) },
        outOfStockCount = alerts.count { it.status.equals("Agotado", ignoreCase = true) },
        alerts = alerts
    )
}
