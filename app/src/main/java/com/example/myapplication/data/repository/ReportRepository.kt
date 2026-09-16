package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Report

open class ReportRepository(
    private val apiService: ApiService
) {

    open suspend fun getReports(): List<Report> {
        return apiService.getReports()
    }

    open suspend fun getReport(id: String): Report {
        return apiService.getReport(id)
    }
}
