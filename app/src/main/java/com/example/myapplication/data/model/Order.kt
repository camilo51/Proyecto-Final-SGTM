package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("clientId")
    val clientId: String,

    @SerializedName("motorcycleId")
    val motorcycleId: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("total")
    val total: Double
)