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

    @SerializedName(value = "diagnostic_notes", alternate = ["problem_description", "description"])
    val description: String = "",

    @SerializedName(value = "assigned_employee_id", alternate = ["assignedEmployeeId"])
    val assignedEmployeeId: String? = null,

    @SerializedName(value = "appointment_id", alternate = ["appointmentId"])
    val appointmentId: String? = null,

    @SerializedName(value = "labor_cost", alternate = ["laborCost"])
    val laborCost: Double? = null,

    @SerializedName("services_cost")
    val servicesCost: Double? = null,

    @SerializedName("parts_cost")
    val partsCost: Double? = null,

    @SerializedName("discount")
    val discount: Double? = null,

    @SerializedName("status")
    val status: String = "",

    @SerializedName(value = "actual_delivery_date", alternate = ["deliveryDate", "date"])
    val deliveryDate: String? = null,

    @SerializedName(value = "final_price", alternate = ["total"])
    val total: Double = 0.0
)
