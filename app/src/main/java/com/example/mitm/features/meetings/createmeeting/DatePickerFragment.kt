package com.example.mitm.features.meetings.createmeeting


import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import com.example.mitm.R
import org.jetbrains.anko.toast
import java.util.*
import java.text.SimpleDateFormat


class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var callback: DateAndTimeFragmentActivityInteraction? = null

    val monthMap = mapOf(
            0 to "Jan", 1 to "Feb", 2 to "Mar", 3 to "Apr", 4 to "May", 5 to "Jun",
            6 to "Jul", 7 to "Aug", 8 to "Sep", 9 to "Oct", 10 to "Nov", 11 to "Dec")


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        val monthStringBuilder = StringBuilder()
        monthStringBuilder.append(monthMap[month])
        monthStringBuilder.append(" $dayOfMonth")
        monthStringBuilder.append(", $year")

        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val date = Date()
        val formattedDate = formatter.format(date)
        val minYear = formattedDate.split('/')[2].toInt()
        val minMonth = formattedDate.split('/')[1].toInt()
        val minDay = formattedDate.split('/')[0].toInt()

        val defaultValue = "$year-$month-$dayOfMonth"

        Log.i("Date", "$minYear $minMonth $minDay")
        Log.i("Date selected", "$year $month $dayOfMonth")
        if (year < minYear || month+1 < minMonth || dayOfMonth < minDay)
            activity?.toast("Date should not be less than today's date!")
        else
        callback?.onDateSelected(monthStringBuilder.toString(), defaultValue)

    }

    fun onDateSelectedListener(activity: Activity) {
        callback = activity as DateAndTimeFragmentActivityInteraction
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }
}
