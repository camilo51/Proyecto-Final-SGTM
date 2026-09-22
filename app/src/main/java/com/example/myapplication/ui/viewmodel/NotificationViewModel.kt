package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.repository.AuditRepository
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: String? = null,
    val orderId: String? = null,
    val inventoryId: Long? = null,
    val statusTransition: StatusTransition? = null
)

data class StatusTransition(
    val previous: String,
    val current: String
)

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val auditRepository: AuditRepository = AuditRepository(),
    private val testScope: CoroutineScope? = null,
    private val inventoryRepository: InventoryRepository? = InventoryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadNotifications() {
        if (_uiState.value.isLoading) return
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val auditResult = auditRepository.getAuditLogs(
                filters = AuditFilters(),
                page = 1,
                limit = NOTIFICATIONS_LIMIT
            )
            val lowStockResult = inventoryRepository?.list(
                InventoryListQuery(status = STOCK_LOW, limit = INVENTORY_NOTIFICATIONS_LIMIT)
            )
            val outOfStockResult = inventoryRepository?.list(
                InventoryListQuery(status = STOCK_OUT, limit = INVENTORY_NOTIFICATIONS_LIMIT)
            )

            val notifications = buildList {
                if (lowStockResult is NetworkResult.Success) {
                    addAll(lowStockResult.data.items.map { inventoryNotification(it, isOutOfStock = false) })
                }
                if (outOfStockResult is NetworkResult.Success) {
                    addAll(outOfStockResult.data.items.map { inventoryNotification(it, isOutOfStock = true) })
                }
                if (auditResult is NetworkResult.Success) {
                    addAll(auditResult.data.items.mapNotNull(AuditLog::operationalNotification))
                }
            }.sortedByDescending { it.createdAt.orEmpty() }

            val hasDataSource = auditResult is NetworkResult.Success ||
                lowStockResult is NetworkResult.Success ||
                outOfStockResult is NetworkResult.Success
            val error = if (hasDataSource) {
                null
            } else {
                (auditResult as? NetworkResult.Error)?.let(::notificationErrorMessage)
                    ?: (lowStockResult as? NetworkResult.Error)?.let(::notificationErrorMessage)
                    ?: (outOfStockResult as? NetworkResult.Error)?.let(::notificationErrorMessage)
                    ?: "No se pudieron cargar las notificaciones."
            }

            _uiState.update {
                it.copy(
                    notifications = notifications,
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }

    private fun notificationErrorMessage(error: NetworkResult.Error): String = when (error.code) {
        401 -> "Tu sesión expiró. Inicia sesión nuevamente."
        403 -> "No tienes permisos para consultar notificaciones."
        else -> when (error.message) {
            "No hay conexión con el servidor.",
            "El servidor tardó demasiado en responder." -> error.message
            else -> "No se pudieron cargar las notificaciones."
        }
    }

    private companion object {
        const val NOTIFICATIONS_LIMIT = 100
        const val INVENTORY_NOTIFICATIONS_LIMIT = 100
        const val STOCK_LOW = "Stock bajo"
        const val STOCK_OUT = "Agotado"
    }
}

private fun AuditLog.operationalNotification(): AppNotification? {
    val normalizedAction = action?.trim()?.uppercase() ?: return null
    if (normalizedAction !in OPERATIONAL_ORDER_ACTIONS && !normalizedAction.contains("SERVICIO")) {
        return null
    }

    val title = when (normalizedAction) {
        "CREAR_ORDEN" -> "Orden creada"
        "CAMBIAR_ESTADO" -> "Estado actualizado"
        else -> if (normalizedAction.contains("AGREGAR") || normalizedAction.contains("CREAR")) {
            "Servicio agregado"
        } else {
            "Servicio actualizado"
        }
    }
    val rawDescription = description.cleanNotificationDescription()
    val statusTransition = rawDescription
        ?.takeIf { normalizedAction == "CAMBIAR_ESTADO" }
        ?.toStatusTransition()
    val description = statusTransition?.let {
        rawDescription?.substringBeforeLast(':')?.trim()?.takeIf(String::isNotBlank)
    } ?: rawDescription
        ?: recordId?.let { "${tableName.readableRecordType()} #$it" }
        ?: "Actualización operativa registrada"

    val notificationId = id ?: "$normalizedAction-$createdAt-$recordId"
    return AppNotification(
        id = "audit-$notificationId",
        title = title,
        description = description,
        createdAt = createdAt,
        orderId = recordId?.takeIf { tableName?.equals("orders", ignoreCase = true) == true },
        statusTransition = statusTransition
    )
}

private fun inventoryNotification(item: InventoryDto, isOutOfStock: Boolean): AppNotification {
    val itemName = item.name?.trim()?.takeIf(String::isNotBlank)
        ?: item.code?.trim()?.takeIf(String::isNotBlank)
        ?: "Repuesto #${item.id}"
    val description = if (isOutOfStock) {
        "$itemName está agotado."
    } else {
        "$itemName tiene ${item.quantity} existencias; su stock está bajo."
    }

    return AppNotification(
        id = "inventory-${item.id}",
        title = if (isOutOfStock) "Stock agotado" else "Stock bajo",
        description = description,
        createdAt = item.updatedAt ?: item.createdAt,
        inventoryId = item.id
    )
}

private fun String?.readableRecordType(): String = when {
    this?.equals("orders", ignoreCase = true) == true -> "Orden de trabajo"
    this?.equals("motorcycles", ignoreCase = true) == true -> "Motocicleta"
    else -> "Registro"
}

private fun String?.cleanNotificationDescription(): String? = this
    ?.replace("\\n", " ")
    ?.replace("\\r", " ")
    ?.replace("\u00E2\u2020\u2019", "→")
    ?.replace(Regex("(?i)['’]n\\.?"), " ")
    ?.replace("\u00E2\u2020\u2019", "\u2192")
    ?.replace(Regex("(?i)'n\\.?"), " ")
    ?.replace(Regex("\\s+"), " ")
    ?.trim()
    ?.takeIf(String::isNotBlank)

private fun String.toStatusTransition(): StatusTransition? {
    val parts = split("→", limit = 2)
    if (parts.size != 2) return null

    val previous = parts[0].substringAfterLast(':').trim()
    val current = parts[1].trim()
    return if (previous.isBlank() || current.isBlank()) null else StatusTransition(previous, current)
}

private val OPERATIONAL_ORDER_ACTIONS = setOf("CREAR_ORDEN", "CAMBIAR_ESTADO")
