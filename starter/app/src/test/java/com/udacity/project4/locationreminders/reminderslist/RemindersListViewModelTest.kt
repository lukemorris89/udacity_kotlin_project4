package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
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
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    lateinit var viewModel: RemindersListViewModel
    lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)
    }

    @Test
    fun loadReminders_remindersToShow_remindersListPopulated() = runBlockingTest {
        //Arrange
        fakeDataSource.deleteAllReminders()
        val reminder = ReminderDTO("", "", "", 0.0, 0.0)
        fakeDataSource.saveReminder(reminder)

        //Act
        viewModel.loadReminders()

        // Assert
        assertEquals(1, viewModel.remindersList.value?.size)
    }

    @Test
    fun showLoading_loadingSetWhileLoading_loadingNotSetWhenCompleted(){
        //Arrange
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()

        // Act / Assert
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())
        mainCoroutineRule.resumeDispatcher()
        assertEquals(false, viewModel.showLoading.getOrAwaitValue())
    }


}