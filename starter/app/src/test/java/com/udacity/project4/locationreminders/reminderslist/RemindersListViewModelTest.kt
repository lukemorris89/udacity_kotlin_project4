package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var viewModel: RemindersListViewModel
    lateinit var dataSource: FakeDataSource

    @Before
    fun setUp() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel((ApplicationProvider.getApplicationContext()), dataSource)
    }

    @Test
    fun noData_showNoDataReturnsTrue() {
        viewModel.loadReminders()
        assertEquals(true, viewModel.showNoData.getOrAwaitValue())
    }

    @Test
    fun remindersAdded_showNoDataReturnsFalse() = runBlockingTest {
        dataSource.saveReminder(ReminderDTO("title1", "description1", "location1", 1.1, 1.1, "id1"))
        viewModel.loadReminders()
        assertEquals(false, viewModel.showNoData.getOrAwaitValue())
        assertEquals(1, viewModel.remindersList.value?.size)
    }
}