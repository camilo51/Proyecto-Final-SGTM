package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Invoice

open class InvoiceRepository(
    private val apiService: ApiService
) {

    open suspend fun getInvoices(): List<Invoice> {
        return apiService.getInvoices()
    }

    open suspend fun getInvoice(id: String): Invoice {
        return apiService.getInvoice(id)
    }

    open suspend fun createInvoice(invoice: Invoice): Invoice {
        return apiService.createInvoice(invoice)
    }

    open suspend fun updateInvoice(id: String, invoice: Invoice): Invoice {
        return apiService.updateInvoice(id, invoice)
    }

    open suspend fun deleteInvoice(id: String) {
        apiService.deleteInvoice(id)
    }
}
