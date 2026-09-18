package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class AuditLog(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("table_name") val tableName: String? = null,
    @SerializedName("record_id") val recordId: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("ip_address") val ipAddress: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AuditFilters(
    val search: String = "",
    val fromDate: String? = null,
    val toDate: String? = null,
    val userId: String? = null,
    val action: String? = null,
    val tableName: String? = null
)
