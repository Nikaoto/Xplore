package com.xplore

import android.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle

/**
 * Created by Nika on 8/3/2017.
 *
 * აღწერა:
 * ზოგადი დროის ამომრჩევი. ვიყენებთ მარტო იმიტომ რომ ტაგების გამოყენების უფლებას იძლევა.
 *
 * Description:
 * Generic time picker. Only used because it allows the use of tags.
 *
 */

class TimePickerFragment(val listener: TimePickerDialog.OnTimeSetListener?) : DialogFragment() {

    //Default time when picking
    private val DEFAULT_HOUR = 12
    private val DEFAULT_MINUTE = 0
    private val IS_24_HOUR_FORMAT = true

    //Empty constructor
    constructor() : this(null)

    override fun onCreateDialog(savedInstanceState: Bundle?)
            = TimePickerDialog(activity, listener, DEFAULT_HOUR, DEFAULT_MINUTE, IS_24_HOUR_FORMAT)
}