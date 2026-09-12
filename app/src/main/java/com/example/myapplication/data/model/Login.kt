package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

// =========================
// LOGIN
// =========================

data class LoginRequest(

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoginData?
)

data class LogoutResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any? = null
)

data class LoginData(

    @SerializedName("user")
    val user: UserDto,

    @SerializedName(value = "access_token", alternate = ["accessToken", "token"])
    val accessToken: String? = null,

    @SerializedName(value = "refresh_token", alternate = ["refreshToken"])
    val refreshToken: String? = null
)


// =========================
// REGISTRO
// =========================

data class RegisterRequest(

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class RegisterResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RegisterData? = null
)

data class RegisterData(

    @SerializedName("user")
    val user: UserDto? = null,

    @SerializedName(value = "access_token", alternate = ["accessToken", "token"])
    val accessToken: String? = null
)


// =========================
// RECUPERAR CONTRASEÑA
// =========================

data class ForgotPasswordRequest(

    @SerializedName("email")
    val email: String
)

data class ForgotPasswordResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)


// =========================
// RESTABLECER CONTRASEÑA
// =========================

data class ResetPasswordRequest(

    @SerializedName("token")
    val token: String,

    @SerializedName("password")
    val password: String
)

data class ResetPasswordResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)


// =========================
// USUARIO DEVUELTO POR LOGIN
// =========================

data class UserDto(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("username")
    val name: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("role", alternate = ["rol", "id_rol", "role_id"])
    val role: String? = null
)


// =========================
// ERRORES
// =========================

data class LoginErrorResponse(

    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("errors")
    val errors: List<ApiError>? = null
)

data class ApiError(

    @SerializedName("message")
    val message: String? = null
)
