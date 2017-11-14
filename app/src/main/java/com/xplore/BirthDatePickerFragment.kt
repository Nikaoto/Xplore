package com.xplore

import android.annotation.SuppressLint
import android.widget.DatePicker
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import java.util.*

@SuppressLint("ValidFragment")
class BirthDatePickerFragment(private val onDataSet: (view: DatePicker, year: Int, month: Int, day: Int) -> Unit)
    : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current Firebase date as the default date in the picker
        TimeManager.refreshGlobalTimeStamp()
        val c = Calendar.getInstance()
        c.time = Date(TimeManager.globalTimeStamp)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) = onDataSet(view, year, month, day)

}