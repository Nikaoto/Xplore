package com.xplore

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import java.util.*

/**
* Created by Nikaoto on 3/11/2017.
*
* აღწერა
* ეს კლასი არის ჩვენი DatePicker DialogFragment-ი, რომელსაც მთელს აპლიკაციაში ვიყენებთ როდესაც
* მომხმარებელს ნებისმიერი თარიღის ამორჩევა ჭირდება
*
* Description:
* This class is a custom datePicker DialogFragment that we use throughout the whole application
* whenever the user needs to select a date
*
*/

class DatePickerFragment(val listener: DatePickerDialog.OnDateSetListener?,
                         val timeStamp: Long,
                         val yearOffset: Int = 0) : DialogFragment() {

    //Empty constructor
    constructor() : this(null, 0L, -1)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Setting up current date
        val c = Calendar.getInstance()
        c.time = Date(timeStamp)
        val year = c.get(Calendar.YEAR) - yearOffset
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, listener, year, month, day)
    }
}
