package com.xplore.base

import android.content.Context
import android.content.Intent

/*
 * Created by Nika on 11/10/2017.
 */

interface BaseView {

    fun getContext(): Context

    fun returnIntent(): Intent

    fun showMessage(msg: String?)

    fun showMessage(msgResId: Int)

    fun showLongMessage(msg: String?)

    fun showLongMessage(msgResId: Int)
}