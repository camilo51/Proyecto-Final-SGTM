package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Motorcycle(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("client_id")
    val clientId: String? = null,

    @SerializedName("plate")
    val plate: String? = null,

    @SerializedName("brand")
    val brand: String? = null,

    @SerializedName("model")
    val model: String? = null,

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("engine_cc")
    val engineCc: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("notes")
    val notes: String? = null
)
