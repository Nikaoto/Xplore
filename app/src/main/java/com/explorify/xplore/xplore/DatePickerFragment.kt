package com.explorify.xplore.xplore

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker

import java.util.Calendar

/**
 * Created by nikao on 3/11/2017.
 */

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //Setting up current date
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) - resources.getInteger(R.integer.limit_age)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, activity as RegisterActivity, year, month, day)
    }
}
