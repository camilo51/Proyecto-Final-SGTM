package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.ui.screens.BackNavigationLink
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal fun BigDecimal.asCurrency(): String = NumberFormat
    .getCurrencyInstance(Locale.forLanguageTag("es-CO"))
    .format(this)

internal fun String?.asReadableDate(): String? {
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    val date = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    ).asSequence().mapNotNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(value)
        }.getOrNull()
    }.firstOrNull() ?: return value.substringBefore('T').substringBefore(' ')

    return SimpleDateFormat(
        "dd 'de' MMMM 'de' yyyy 'a las' HH:mm",
        Locale("es", "CO")
    ).format(date)
}

@Composable
internal fun InventoryBackLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackNavigationLink(onClick = onClick, modifier = modifier)
}

@Composable
internal fun InventoryStatusChip(status: String) {
    val normalized = status.lowercase(Locale.ROOT)
    val (containerColor, contentColor) = when {
        normalized.contains("agot") -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        normalized.contains("bajo") -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFD1FAE5) to Color(0xFF065F46)
    }
    Surface(color = containerColor, contentColor = contentColor, shape = RoundedCornerShape(50)) {
        Text(
            text = status.ifBlank { "Sin estado" },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun InventoryMessage(
    modifier: Modifier = Modifier,
    title: String,
    detail: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
internal fun InventorySummary(item: InventoryDto) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name ?: "Repuesto sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(item.code, item.brand, item.category)
                        .filter { it.isNotBlank() }
                        .joinToString(" - ")
                        .ifBlank { "Sin c\u00f3digo ni clasificaci\u00f3n" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            InventoryStatusChip(item.status)
        }

        InventoryDataCell(
            label = "Existencias",
            value = "${item.quantity} ${item.unit?.takeIf { it.isNotBlank() } ?: "und."}",
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            InventoryPrice(
                label = "Precio de venta",
                value = item.salePrice.asCurrency(),
                modifier = Modifier.weight(1f)
            )
            InventoryPrice(
                label = "Costo unitario",
                value = item.unitPrice.asCurrency(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InventoryDataCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun InventoryPrice(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
