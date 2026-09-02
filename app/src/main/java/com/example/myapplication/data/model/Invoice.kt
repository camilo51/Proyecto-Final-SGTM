package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Invoice(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("clientId")
    val clientId: String,

    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("total")
    val total: Double,

    @SerializedName("status")
    val status: String
)