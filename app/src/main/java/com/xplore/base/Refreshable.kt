package com.xplore.base

import android.view.View

/*
 * Created by Nika on 11/11/2017.
 */

interface Refreshable {

    // TODO add shouldrefresh boolean (to initRefreshLayout?)

    fun initRefreshLayout(view: View, layoutResId: Int)

    fun setRefreshing(isRefreshing: Boolean)

    fun onRefreshed()

}