package com.example.mitm.features.meetings.createmeeting


import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import com.example.mitm.R
import java.lang.StringBuilder
import java.util.*


class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var callback: DateAndTimeFragmentActivityInteraction

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        var hour = hourOfDay
        var time: String
        var amOrPm: String

        if (hour >= 12)  {

            hour = (hour - 12)
            amOrPm = "PM"
        }

        else amOrPm = "AM"
        if (hour == 0) {
            hour = 12
        }

       time = when {

           minute < 10 -> "$hour:0$minute $amOrPm"
           else -> "$hour:$minute $amOrPm"
       }
        callback.onTimeSelected(time)
    }

    fun setOnTimeSelectedListener(activity: Activity) {
        callback = activity as DateAndTimeFragmentActivityInteraction
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }


}
