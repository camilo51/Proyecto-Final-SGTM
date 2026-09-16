package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Invoice(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName(value = "client_id", alternate = ["clientId"])
    val clientId: String = "",

    @SerializedName(value = "order_id", alternate = ["orderId"])
    val orderId: String = "",

    @SerializedName(value = "issue_date", alternate = ["date", "created_at"])
    val date: String? = null,

    @SerializedName(value = "total", alternate = ["final_price", "amount"])
    val total: Double = 0.0,

    @SerializedName("status")
    val status: String = ""
)
