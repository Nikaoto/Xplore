package com.xplore.base.refreshable

import android.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.xplore.R
import com.xplore.base.refreshable.Refreshable

/*
 * Created by Nika on 11/11/2017.
 */

abstract class RefreshableFragment : Fragment(), Refreshable {

    private var refreshLayout: SwipeRefreshLayout? = null

    override var shouldRefreshOnResume: Boolean = false
    private var allowRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initRefreshLayout(layout: SwipeRefreshLayout, shouldRefreshOnResume: Boolean) {
        this.shouldRefreshOnResume = shouldRefreshOnResume

        refreshLayout = layout
        refreshLayout?.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2,
                R.color.refresh_color_3)
        refreshLayout?.setOnRefreshListener {
            onRefreshed()
        }
    }

    override fun initRefreshLayout(layout: SwipeRefreshLayout) {
        initRefreshLayout(layout, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_refresh) {
            onRefreshed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setLoading(isLoading: Boolean) {
        refreshLayout?.isRefreshing = isLoading
    }

    override fun onRefreshed() {}

    override fun onResume() {
        super.onResume()
        if (shouldRefreshOnResume) {
            refreshOnResume()
        }
    }

    override fun refreshOnResume() {
        if (allowRefresh) {
            allowRefresh = false
            onRefreshed()
        } else {
            allowRefresh = true
        }
    }
}