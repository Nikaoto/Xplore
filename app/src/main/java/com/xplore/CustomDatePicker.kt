package com.xplore

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import java.util.*

/**
 * Created by nikao on 3/11/2017.
 */

class CustomDatePicker(val listener: DatePickerDialog.OnDateSetListener?,
                       val timeStamp: Long,
                       val yearOffset: Int = 0) : DialogFragment() {

    //Empty constructor
    constructor() : this(null, 0L, -1)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        //Setting up current date
        val c = Calendar.getInstance()
        c.time = Date(timeStamp)
        val year = c.get(Calendar.YEAR) - yearOffset
        val month = c.get(Calendar.MONTH) + 1 //+1 is necessary because 0 is january
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, listener, year, month, day)
    }
}
