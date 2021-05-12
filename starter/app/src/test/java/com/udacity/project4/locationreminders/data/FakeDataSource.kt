package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: ArrayList<ReminderDTO> = ArrayList()) : ReminderDataSource {

    var errorStatus = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        return if (errorStatus) Result.Error("Error") else Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remindersList.find { it.id == id }
        return if (errorStatus) {
            Result.Error("Error")
        } else {
            Result.Success(reminder as ReminderDTO)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }


}