package com.example.myapplication.data.model.inventory

import com.example.myapplication.data.model.common.PaginationDto
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class InventoryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("quantity") val quantity: Int = 0,
    @SerializedName("min_stock") val minStock: Int = 0,
    @SerializedName("unit_price") val unitPrice: BigDecimal = BigDecimal.ZERO,
    @SerializedName("sale_price") val salePrice: BigDecimal = BigDecimal.ZERO,
    @SerializedName("supplier") val supplier: String? = null,
    @SerializedName("sold_count") val soldCount: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class InventoryMovementDto(
    @SerializedName("id") val id: Long,
    @SerializedName("movement_type") val movementType: String? = null,
    @SerializedName("quantity") val quantity: Int = 0,
    @SerializedName("quantity_before") val quantityBefore: Int? = null,
    @SerializedName("quantity_after") val quantityAfter: Int? = null,
    @SerializedName("reference_type") val referenceType: String? = null,
    @SerializedName("reference_id") val referenceId: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("created_by_name") val createdByName: String? = null
)

data class StockMovementResultDto(
    @SerializedName("quantityBefore") val quantityBefore: Int? = null,
    @SerializedName("quantityAfter") val quantityAfter: Int? = null,
    @SerializedName("delta") val delta: Int? = null,
    @SerializedName("item") val item: InventoryDto? = null
)

/**
 * El endpoint está protegido; soporta el envoltorio de item usado por la API sin asumir
 * campos adicionales. El cliente puede abrir el detalle cuando inventoryId está disponible.
 */
data class InventoryAlertDto(
    @SerializedName("inventory_id") val inventoryId: Long? = null,
    @SerializedName("item") val item: InventoryDto? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null
)

data class InventoryPartReference(
    val id: Long,
    val code: String?,
    val name: String?,
    val brand: String?,
    val salePrice: BigDecimal,
    val quantity: Int,
    val status: String
)

data class InventoryListQuery(
    val search: String = "",
    val category: String? = null,
    val brand: String? = null,
    val status: String? = null,
    val sort: InventorySort = InventorySort.NAME,
    val page: Int = 1,
    val limit: Int = 20
)

enum class InventorySort(val apiValue: String, val label: String) {
    NAME("name", "Nombre"),
    CODE("code", "Código"),
    CATEGORY("category", "Categoría"),
    QUANTITY("quantity", "Stock"),
    SOLD_COUNT("sold_count", "Más vendidos"),
    CREATED_AT("created_at", "Más recientes")
}

data class InventoryAlertSummary(
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val alerts: List<InventoryAlertDto> = emptyList()
)

data class InventoryMovementsPage(
    val movements: List<InventoryMovementDto>,
    val pagination: PaginationDto
)
