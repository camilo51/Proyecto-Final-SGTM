package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceRequest(
    @SerializedName(value = "order_id", alternate = ["orderId"])
    val orderId: String,

    @SerializedName(value = "payment_method", alternate = ["paymentMethod"])
    val paymentMethod: String,

    @SerializedName("notes")
    val notes: String? = null
)

data class Invoice(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName(value = "invoice_number", alternate = ["invoiceNumber", "number", "code"])
    val invoiceNumber: String? = null,

    @SerializedName(value = "client_id", alternate = ["clientId"])
    val clientId: String = "",

    @SerializedName(value = "order_id", alternate = ["orderId"])
    val orderId: String = "",

    @SerializedName(value = "issue_date", alternate = ["date", "created_at"])
    val date: String? = null,

    @SerializedName(value = "total", alternate = ["final_price", "amount"])
    val total: Double = 0.0,

    @SerializedName(value = "payment_method", alternate = ["paymentMethod"])
    val paymentMethod: String = "",

    @SerializedName(value = "notes", alternate = ["note"])
    val notes: String? = null,

    @SerializedName(value = "payment_status", alternate = ["paymentStatus"])
    val paymentStatus: String = "",

    @SerializedName("status")
    val status: String = ""
)
