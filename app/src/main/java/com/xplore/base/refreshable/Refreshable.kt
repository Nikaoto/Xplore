package com.xplore.base.refreshable

import android.support.v4.widget.SwipeRefreshLayout

/*
 * Created by Nika on 11/11/2017.
 */

interface Refreshable {

    // TODO add shouldRefreshOnResume boolean (to initRefreshLayout?)

    fun initRefreshLayout(layout: SwipeRefreshLayout)

    fun setLoading(isLoading: Boolean)

    fun onRefreshed()

}