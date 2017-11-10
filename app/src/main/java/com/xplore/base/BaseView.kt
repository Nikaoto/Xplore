package com.xplore.base

import android.content.Context

/*
 * Created by Nika on 11/10/2017.
 */

interface BaseView {

    fun getContext(): Context

    fun showMessage(msg: String?)

    fun showMessage(msgResId: Int)

    fun showLongMessage(msg: String?)

    fun showLongMessage(msgResId: Int)
}