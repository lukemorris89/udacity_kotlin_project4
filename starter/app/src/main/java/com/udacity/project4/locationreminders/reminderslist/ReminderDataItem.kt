package com.udacity.project4.locationreminders.reminderslist

import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize

/**
 * data class acts as a data mapper between the DB and the UI
 */

@Parcelize
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Parcelable