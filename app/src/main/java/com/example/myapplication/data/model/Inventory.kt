package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Inventory(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("brandId")
    val brandId: String? = null
)