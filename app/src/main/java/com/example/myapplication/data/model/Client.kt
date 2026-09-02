package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Client(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String? = null
)