package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.common.requireData
import com.google.gson.JsonNull
import com.google.gson.JsonObject

open class MotorcycleRepository(
    private val apiService: ApiService
) {
    open suspend fun getMotorcycles(): List<Motorcycle> {
        return apiService.getMotorcycles(limit = 100).requireData()
    }

    open suspend fun getMotorcyclesForClient(clientId: String): List<Motorcycle> {
        return apiService.getMotorcycles(clientId = clientId, limit = 100).requireData()
    }

    open suspend fun getMotorcycle(id: String): Motorcycle {
        return apiService.getMotorcycle(id).requireData()
    }

    open suspend fun createMotorcycle(motorcycle: Motorcycle): Motorcycle {
        return apiService.createMotorcycle(motorcycle.toApiJson()).requireData()
    }

    open suspend fun updateMotorcycle(id: String, motorcycle: Motorcycle): Motorcycle {
        return apiService.updateMotorcycle(id, motorcycle.toApiJson()).requireData()
    }

    open suspend fun deleteMotorcycle(id: String) {
        apiService.deleteMotorcycle(id)
    }

    private fun Motorcycle.toApiJson(): JsonObject = JsonObject().apply {
        addNullableClientId(clientId)
        addNullableProperty("plate", plate)
        addNullableProperty("brand", brand)
        addNullableProperty("model", model)
        addNullableProperty("year", year)
        addNullableProperty("color", color)
        addNullableProperty("engine_cc", engineCc)
        addNullableProperty("status", status)
        addNullableProperty("notes", notes)
    }

    private fun JsonObject.addNullableProperty(name: String, value: String?) {
        if (value == null) add(name, JsonNull.INSTANCE) else addProperty(name, value)
    }

    private fun JsonObject.addNullableProperty(name: String, value: Int?) {
        if (value == null) add(name, JsonNull.INSTANCE) else addProperty(name, value)
    }

    private fun JsonObject.addNullableClientId(clientId: String?) {
        when {
            clientId == null -> add("client_id", JsonNull.INSTANCE)
            clientId.toLongOrNull() != null -> addProperty("client_id", clientId.toLong())
            else -> addProperty("client_id", clientId)
        }
    }
}
