package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class User(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName(value = "username", alternate = ["name"])
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null
)
