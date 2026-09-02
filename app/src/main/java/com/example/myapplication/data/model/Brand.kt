package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Brand(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null
)