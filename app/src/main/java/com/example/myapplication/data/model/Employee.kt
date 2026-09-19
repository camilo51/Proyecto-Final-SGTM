package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Employee(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName(value = "last_name", alternate = ["lastName"])
    val lastName: String = "",

    @SerializedName("specialty")
    val specialty: String = "",

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("status")
    val status: String = "Activo"
)
