package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.common.requireData

class EmployeeRepository(
    private val apiService: ApiService
) {

    suspend fun getEmployees(): List<Employee> {
        return apiService.getEmployees().requireData()
    }

    suspend fun getEmployee(id: String): Employee {
        return apiService.getEmployee(id).requireData()
    }

    suspend fun createEmployee(employee: Employee): Employee {
        return apiService.createEmployee(employee).requireData()
    }

    suspend fun updateEmployee(
        id: String,
        employee: Employee
    ): Employee {
        return apiService.updateEmployee(id, employee).requireData()
    }

    suspend fun deleteEmployee(id: String) {
        apiService.deleteEmployee(id)
    }
}