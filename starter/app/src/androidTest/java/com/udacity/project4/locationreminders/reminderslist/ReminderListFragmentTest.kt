package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setUp() {
        datasource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), datasource)
    }

    @Test
    fun showListOfReminders() = runBlockingTest {
        val remindersList =
            listOf(
                ReminderDTO("title1", "description1", "location1", 1.1, 1.1),
                ReminderDTO("title2", "description2", "location2", 2.2, 2.2)
            )

        remindersList.forEach {
            datasource.saveReminder(it)
        }

        val fetchedReminders = datasource.getReminders()

        onView(withId(R.id.reminderssRecyclerView))

    }
}