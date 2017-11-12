package com.xplore.base.refreshable

import android.support.v4.widget.SwipeRefreshLayout

/*
 * Created by Nika on 11/11/2017.
 */

interface Refreshable {

    var shouldRefreshOnResume: Boolean

    fun initRefreshLayout(layout: SwipeRefreshLayout)

    fun initRefreshLayout(layout: SwipeRefreshLayout, shouldRefreshOnResume: Boolean)

    fun setLoading(isLoading: Boolean)

    fun refreshOnResume()

    fun onRefreshed()

}