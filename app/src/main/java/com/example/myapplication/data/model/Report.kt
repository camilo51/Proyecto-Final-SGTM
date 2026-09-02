package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Report(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("date")
    val date: String
)