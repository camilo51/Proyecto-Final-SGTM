package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Client(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("document_type")
    val documentType: String? = "CC",

    @SerializedName("document")
    val document: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("status")
    val status: String? = "Activo"
)

/** Request fields supported by POST/PUT /clients. */
data class ClientRequest(
    @SerializedName("document_type")
    val documentType: String = "CC",

    @SerializedName("document")
    val document: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("phone")
    val phone: String? = null
)

data class DeleteClientRequest(
    @SerializedName("reason")
    val reason: String
)
