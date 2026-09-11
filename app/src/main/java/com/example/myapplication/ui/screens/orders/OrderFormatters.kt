package com.example.myapplication.ui.screens.orders

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatOrderMoney(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
    formatter.currency = Currency.getInstance("COP")
    formatter.maximumFractionDigits = 0
    return formatter.format(value)
}

@Composable
fun orderStatusColor(status: String): Color {
    val normalized = status.lowercase(Locale.ROOT)
    return when {
        normalized.contains("entreg") || normalized.contains("lista") -> MaterialTheme.colorScheme.primary
        normalized.contains("repar") || normalized.contains("proceso") -> MaterialTheme.colorScheme.tertiary
        normalized.contains("esper") || normalized.contains("pend") -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
