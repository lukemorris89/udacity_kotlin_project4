package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class FakeRemindersDao: RemindersDao {

    val remindersList = mutableListOf<ReminderDTO>()
    var errorThrown = false

    override suspend fun getReminders(): List<ReminderDTO> {
        if (errorThrown) throw (Exception ("Test error")) else return remindersList

    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        if (errorThrown) throw (Exception ("Test error"))
        else
            return remindersList.firstOrNull { it.id == reminderId }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}