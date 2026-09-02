package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginData? = null
)

data class LoginErrorResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("errors") val errors: List<LoginFieldError>? = null
)

data class LoginFieldError(
    @SerializedName("field") val field: String? = null,
    @SerializedName("message") val message: String? = null
)

data class LoginData(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("status") val status: String,
    @SerializedName("last_login") val lastLogin: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("role") val role: String
)
