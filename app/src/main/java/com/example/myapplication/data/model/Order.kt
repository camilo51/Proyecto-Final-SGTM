package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Order(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName(value = "order_number", alternate = ["orderNumber"])
    val orderNumber: String? = null,

    @SerializedName(value = "client_id", alternate = ["clientId"])
    val clientId: String = "",

    @SerializedName(value = "motorcycle_id", alternate = ["motorcycleId"])
    val motorcycleId: String = "",

    @SerializedName(value = "diagnostic_notes", alternate = ["description"])
    val description: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName(value = "final_price", alternate = ["total"])
    val total: Double = 0.0
)
