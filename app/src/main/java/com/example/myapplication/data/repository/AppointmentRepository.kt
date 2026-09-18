package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Appointment

open class AppointmentRepository(
    private val apiService: ApiService
) {

    open suspend fun getAppointments(): List<Appointment> {
        return apiService.getAppointments()
    }

    open suspend fun getAppointment(id: String): Appointment {
        return apiService.getAppointment(id)
    }

    open suspend fun createAppointment(appointment: Appointment): Appointment {
        return apiService.createAppointment(appointment)
    }

    open suspend fun updateAppointment(id: String, appointment: Appointment): Appointment {
        return apiService.updateAppointment(id, appointment)
    }

    open suspend fun deleteAppointment(id: String) {
        apiService.deleteAppointment(id)
    }
}
