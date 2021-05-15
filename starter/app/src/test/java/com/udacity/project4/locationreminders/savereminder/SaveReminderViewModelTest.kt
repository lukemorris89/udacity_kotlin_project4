package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import junit.framework.Assert.assertEquals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var viewModel: SaveReminderViewModel
    lateinit var dataSource: FakeDataSource

    @Before
    fun setUp() {
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun onCreateViewModel_noDataEntered_returnNull() {
        assertEquals(null, viewModel.reminderTitle.value)
        assertEquals(null, viewModel.reminderDescription.value)
        assertEquals(null, viewModel.reminderSelectedLocationStr.value)
        assertEquals(null, viewModel.selectedPOI.value)
        assertEquals(null, viewModel.latitude.value)
        assertEquals(null, viewModel.longitude.value)
    }

    @Test
    fun onSaveReminder_loadingShown_loadingHiddenOnceCompleted() {
        val reminder = ReminderDataItem("title", "description", "location", 1.1, 1.1)
        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(reminder)
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())
        mainCoroutineRule.resumeDispatcher()
        assertEquals(false, viewModel.showLoading.getOrAwaitValue())

    }
}