package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(context, RemindersDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        remindersDao = database.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun noRemindersSet_getReminders_returnsZero() = runBlockingTest {
        val reminders = remindersDao.getReminders()

        assertEquals(0, reminders.size)
    }

    @Test
    fun saveTwoReminders_getReminders_returnsTwoReminders() = runBlockingTest {
        val remindersList = listOf(
            ReminderDTO("title1", "description1", "location1", 1.1, 1.1, "id1"),
            ReminderDTO("title2", "description2", "location2", 2.2, 2.2, "id2")
        )

        remindersList.forEach {
            remindersDao.saveReminder(it)
        }

        val reminders = remindersDao.getReminders()

        assertEquals(2, reminders.size)
    }

    @Test
    fun saveTwoReminders_getSecondReminder_returnsSecondReminder() = runBlockingTest {
        val remindersList = listOf(
            ReminderDTO("title1", "description1", "location1", 1.1, 1.1, "id1"),
            ReminderDTO("title2", "description2", "location2", 2.2, 2.2, "id2")
        )

        remindersList.forEach {
            remindersDao.saveReminder(it)
        }

        val reminder = remindersDao.getReminderById("id2")
        assertEquals("title2", reminder?.title)
    }

    @Test
    fun saveTwoReminders_deleteAllReminders_returnsZero() = runBlockingTest {
        val remindersList = listOf(
            ReminderDTO("title1", "description1", "location1", 1.1, 1.1, "id1"),
            ReminderDTO("title2", "description2", "location2", 2.2, 2.2, "id2")
        )

        remindersList.forEach {
            remindersDao.saveReminder(it)
        }

        remindersDao.deleteAllReminders()

        val reminders = remindersDao.getReminders()
        assertEquals(0, reminders.size)
    }

}