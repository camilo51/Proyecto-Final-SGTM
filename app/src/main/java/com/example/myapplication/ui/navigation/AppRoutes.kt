package com.example.myapplication.ui.navigation

import android.net.Uri

object AppRoutes {
    const val Login = "login"
    const val Admin = "admin"
    const val Home = "home"
    const val Clients = "clients"
    const val Orders = "orders"
    const val OrderDetail = "orders/{orderId}"
    const val CreateOrder = "orders/create"
    const val EditOrder = "orders/{orderId}/edit"

    fun orderDetail(id: String): String = "orders/${Uri.encode(id)}"
    fun editOrder(id: String): String = "orders/${Uri.encode(id)}/edit"
}
