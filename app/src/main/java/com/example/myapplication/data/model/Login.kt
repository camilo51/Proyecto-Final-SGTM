package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    @SerializedName("token_type")
    val tokenType: String? = null,

    @SerializedName("user")
    val user: User? = null
)