package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Reminder(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("clientId")
    val clientId: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("status")
    val status: String
)