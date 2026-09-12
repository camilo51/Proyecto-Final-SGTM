package com.example.myapplication.data.api

import com.example.myapplication.data.model.ApiError
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("errors")
    val errors: List<ApiError>? = null
)

class ApiException(
    val statusCode: Int? = null,
    override val message: String
) : Exception(message)
