package com.example.myapplication.data.repository.inventory

object InventoryHttpErrorPolicy {
    fun isUnauthorized(statusCode: Int): Boolean = statusCode == 401

    fun messageFor(statusCode: Int, serverMessage: String? = null): String {
        return serverMessage?.takeIf { it.isNotBlank() } ?: when (statusCode) {
            403 -> "No tienes permisos para realizar esta acción."
            404 -> "El repuesto solicitado no existe."
            409 -> "La operación entra en conflicto con el historial de inventario."
            else -> "El servidor rechazó la solicitud ($statusCode)."
        }
    }

    fun isSuccessfulDelete(statusCode: Int): Boolean = statusCode in 200..299
}
