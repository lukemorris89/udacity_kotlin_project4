package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeRemindersDao: FakeRemindersDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    val remindersList = listOf(
        ReminderDTO("title1", "description1", "location1", 1.1, 1.1, "id1"),
        ReminderDTO("title2", "description2", "location2", 2.2, 2.2, "id2")
    )

    @Before
    fun setup() {
        fakeRemindersDao = FakeRemindersDao()
        remindersLocalRepository = RemindersLocalRepository(
            fakeRemindersDao, Dispatchers.Unconfined
        )
        fakeRemindersDao.remindersList.addAll(remindersList)
    }

    @Test
    fun getReminders_returnsTwoReminders() = runBlockingTest {
        val reminders = (remindersLocalRepository.getReminders() as Result.Success).data

        assertEquals(2, reminders.size)
    }

    @Test
    fun getReminders_errorSet_returnsErrorMessage() = runBlockingTest {
        fakeRemindersDao.errorThrown = true

        val error = (remindersLocalRepository.getReminders() as Result.Error).message

        assertEquals("Test error", error)
    }

    @Test
    fun getRemindersById_returnsFirstReminder() = runBlockingTest {
        val reminder = (remindersLocalRepository.getReminder("id1") as Result.Success).data

        assertEquals("title1", reminder.title)
    }

    @Test
    fun saveReminder_canBeFetched() = runBlockingTest {
        val reminder = ReminderDTO("title3", "description3", "location3", 3.3, 3.3, "id3")
        remindersLocalRepository.saveReminder(reminder)

        val fetchedReminder = (remindersLocalRepository.getReminder("id3") as Result.Success).data
        assertEquals("title3", fetchedReminder.title)
    }

    @Test
    fun deleteReminders_getAllReminders_returnsEmptyList() = runBlockingTest {
        remindersLocalRepository.deleteAllReminders()

        val reminders = (remindersLocalRepository.getReminders() as Result.Success).data

        assertEquals(true, reminders.isEmpty())
    }
}