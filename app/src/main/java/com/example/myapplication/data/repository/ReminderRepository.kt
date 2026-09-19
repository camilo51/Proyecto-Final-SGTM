package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Reminder

open class ReminderRepository(
    private val apiService: ApiService
) {

    open suspend fun getReminders(): List<Reminder> {
        return apiService.getReminders()
    }

    open suspend fun getReminder(id: String): Reminder {
        return apiService.getReminder(id)
    }

    open suspend fun createReminder(reminder: Reminder): Reminder {
        return apiService.createReminder(reminder)
    }

    open suspend fun updateReminder(id: String, reminder: Reminder): Reminder {
        return apiService.updateReminder(id, reminder)
    }

    open suspend fun deleteReminder(id: String) {
        apiService.deleteReminder(id)
    }
}
