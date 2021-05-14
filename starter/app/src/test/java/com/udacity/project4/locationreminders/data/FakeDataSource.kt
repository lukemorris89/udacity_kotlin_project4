package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    val remindersList = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (remindersList.size != 0)
            Result.Success(remindersList)
        else Result.Error("No reminders saved")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val found = remindersList.firstOrNull { it.id == id }
        return if (found != null) {
            Result.Success(found)
        } else {
            Result.Error("Reminder with that ID not found", -1)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}