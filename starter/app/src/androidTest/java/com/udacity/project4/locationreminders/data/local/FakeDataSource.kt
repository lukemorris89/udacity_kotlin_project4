package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        remindersList?.let {
            return Result.Success(it)
        }
        return Result.Error("No reminders saved")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val found = remindersList?.firstOrNull { it.id == id }
        return if (found != null) {
            Result.Success(found)
        } else {
            Result.Error("Reminder with that ID not found", -1)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }
}