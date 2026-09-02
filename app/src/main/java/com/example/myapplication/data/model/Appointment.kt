package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Appointment(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("clientId")
    val clientId: String,

    @SerializedName("motorcycleId")
    val motorcycleId: String,

    @SerializedName("employeeId")
    val employeeId: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("status")
    val status: String
)