package com.example.myapplication.data.model.common

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<ApiError> = emptyList()
)

data class PaginatedApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationDto? = null,
    @SerializedName("errors") val errors: List<ApiError> = emptyList()
)

data class PaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class ApiError(
    @SerializedName("field") val field: String? = null,
    @SerializedName("message") val message: String? = null
)

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T, val message: String = "") : NetworkResult<T>
    data class Error(
        val message: String,
        val code: Int? = null,
        val errors: List<ApiError> = emptyList()
    ) : NetworkResult<Nothing>

}

data class PaginatedResult<T>(
    val items: List<T>,
    val pagination: PaginationDto
)
