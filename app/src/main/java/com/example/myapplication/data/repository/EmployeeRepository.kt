package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.common.requireData

open class EmployeeRepository(
    private val apiService: ApiService
) {

    open suspend fun getEmployees(): List<Employee> {
        return apiService.getEmployees().requireData().items
    }

    suspend fun getEmployee(id: String): Employee {
        return apiService.getEmployee(id)
    }

    suspend fun createEmployee(employee: Employee): Employee {
        return apiService.createEmployee(employee)
    }

    suspend fun updateEmployee(
        id: String,
        employee: Employee
    ): Employee {
        return apiService.updateEmployee(id, employee)
    }

    suspend fun deleteEmployee(id: String) {
        apiService.deleteEmployee(id)
    }
}
