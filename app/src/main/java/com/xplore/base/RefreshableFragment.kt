package com.xplore.base

import android.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import com.xplore.R

/*
 * Created by Nika on 11/11/2017.
 */

abstract class RefreshableFragment : Fragment(), Refreshable {

    private var refreshLayout: SwipeRefreshLayout? = null

    override fun initRefreshLayout(view: View, layoutResId: Int) {
        refreshLayout = view.findViewById<SwipeRefreshLayout>(layoutResId)
        refreshLayout?.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2,
                R.color.refresh_color_3)
        refreshLayout?.setOnRefreshListener {
            onRefreshed()
        }
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        refreshLayout?.isRefreshing = isRefreshing
    }

    override fun onRefreshed() {}
}