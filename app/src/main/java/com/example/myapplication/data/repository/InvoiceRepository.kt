package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Invoice

class InvoiceRepository(
    private val apiService: ApiService
) {
    suspend fun getInvoices(): List<Invoice> {
        return apiService.getInvoices()
    }

    suspend fun getInvoice(id: String): Invoice {
        return apiService.getInvoice(id)
    }

    suspend fun createInvoice(invoice: Invoice): Invoice {
        return apiService.createInvoice(invoice)
    }

    suspend fun updateInvoice(id: String, invoice: Invoice): Invoice {
        return apiService.updateInvoice(id, invoice)
    }

    suspend fun deleteInvoice(id: String) {
        apiService.deleteInvoice(id)
    }
}
