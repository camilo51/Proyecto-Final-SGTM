package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Motorcycle(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("plate")
    val plate: String,

    @SerializedName("clientId")
    val clientId: String
)