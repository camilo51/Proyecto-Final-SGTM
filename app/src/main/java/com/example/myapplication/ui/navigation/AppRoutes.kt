package com.example.myapplication.ui.navigation

import android.net.Uri

object AppRoutes {
    const val Login = "login"
    const val Admin = "admin"
    const val Profile = "profile"
    const val Home = "home"
    const val Clients = "clients"
    const val Motorcycles = "motorcycles"
    const val MotorcycleDetail = "motorcycles/{motorcycleId}"
    const val CreateMotorcycle = "motorcycles/create"
    const val EditMotorcycle = "motorcycles/{motorcycleId}/edit"
    const val Inventory = "inventory"
    const val Orders = "orders"
    const val Invoices = "invoices"
    const val InvoiceDetail = "invoices/{invoiceId}"
    const val Reports = "reports"
    const val OrderDetail = "orders/{orderId}"
    const val CreateOrder = "orders/create"
    const val EditOrder = "orders/{orderId}/edit"

    fun orderDetail(id: String): String = "orders/${Uri.encode(id)}"
    fun editOrder(id: String): String = "orders/${Uri.encode(id)}/edit"
    fun motorcycleDetail(id: String): String = "motorcycles/${Uri.encode(id)}"
    fun editMotorcycle(id: String): String = "motorcycles/${Uri.encode(id)}/edit"
    fun invoiceDetail(id: String): String = "invoices/${Uri.encode(id)}"
}
