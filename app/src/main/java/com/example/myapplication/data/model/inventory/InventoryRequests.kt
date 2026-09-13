package com.example.myapplication.data.model.inventory

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateInventoryRequest(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("min_stock") val minStock: Int? = null,
    @SerializedName("unit_price") val unitPrice: BigDecimal? = null,
    @SerializedName("sale_price") val salePrice: BigDecimal? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("supplier") val supplier: String? = null
)

data class UpdateInventoryRequest(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("min_stock") val minStock: Int? = null,
    @SerializedName("unit_price") val unitPrice: BigDecimal? = null,
    @SerializedName("sale_price") val salePrice: BigDecimal? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("supplier") val supplier: String? = null
)

data class StockEntryRequest(
    @SerializedName("qty") val quantity: Int,
    @SerializedName("notes") val notes: String
)

data class StockOutputRequest(
    @SerializedName("qty") val quantity: Int,
    @SerializedName("notes") val notes: String
)

data class StockAdjustmentRequest(
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("notes") val notes: String
)

data class DeleteInventoryRequest(
    @SerializedName("reason") val reason: String
)

enum class StockMovementAction(val apiValue: String, val label: String) {
    ENTRY("entry", "Entrada"),
    OUTPUT("output", "Salida"),
    ADJUSTMENT("adjustment", "Ajuste")
}
