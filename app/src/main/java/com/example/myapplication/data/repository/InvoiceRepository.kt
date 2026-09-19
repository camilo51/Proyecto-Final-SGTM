package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.InvoiceRequest
import com.example.myapplication.data.model.common.requireData

class InvoiceRepository(
    private val apiService: ApiService
) {
    suspend fun getInvoices(): List<Invoice> {
        return apiService.getInvoices().requireData().items
    }

    suspend fun getInvoice(id: String): Invoice {
        return apiService.getInvoice(id).requireData()
    }

    suspend fun createInvoice(request: InvoiceRequest): Invoice {
        return apiService.createInvoice(request).requireData()
    }

    suspend fun payInvoice(id: String): Invoice {
        return apiService.payInvoice(id).requireData()
    }

    suspend fun cancelInvoice(id: String): Invoice {
        return apiService.cancelInvoice(id).requireData()
    }

    suspend fun deleteInvoice(id: String) {
        apiService.deleteInvoice(id)
    }
}
