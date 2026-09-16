package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Invoice
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

    suspend fun createInvoice(invoice: Invoice): Invoice {
        return apiService.createInvoice(invoice).requireData()
    }

    suspend fun updateInvoice(id: String, invoice: Invoice): Invoice {
        return apiService.updateInvoice(id, invoice).requireData()
    }

    suspend fun deleteInvoice(id: String) {
        apiService.deleteInvoice(id)
    }
}
