package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var appContext: Application

    @get:Rule
    val activityRule = object : ActivityTestRule<RemindersActivity>(RemindersActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            stopKoin()
            val appContext: Application = getApplicationContext()
            val testModule = module {
                viewModel {
                    RemindersListViewModel(
                        appContext,
                        get() as ReminderDataSource
                    )
                }
                single {
                    SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                    )
                }
                single { RemindersLocalRepository(get()) as ReminderDataSource }
                single { LocalDB.createRemindersDao(appContext) }
            }
            startKoin {
                androidContext(getApplicationContext())
                modules(testModule)
            }
            dataSource = get()
            viewModel = get()
        }
    }

    @Test
    fun deleteAllReminders_showsSnackbarErrorMessage() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.refreshLayout)).perform(swipeDown())
        onView(withText("No reminders found")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun saveReminder_reminderDisplayed() = runBlocking {
        val reminder = ReminderDTO(
            "test title1",
            "description1",
            "location1",
            1.1,
            1.1
        )
        dataSource.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText("test title1")).check(matches(isDisplayed()))

        dataSource.deleteAllReminders()
    }

    @Test
    fun pressFab_navigatesToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}