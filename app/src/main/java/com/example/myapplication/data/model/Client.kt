package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Client(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String = "",

    // La UI conserva el nombre "cedula", pero el backend usa "document".
    @SerializedName("document")
    val cedula: String? = null,

    @SerializedName("email")
    val email: String = "",

    @SerializedName("phone")
    val phone: String = ""
)
