package com.xplore.base

import android.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import com.xplore.R

/*
 * Created by Nika on 11/11/2017.
 */

abstract class RefreshableFragment : Fragment(), Refreshable {

    private var refreshLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initRefreshLayout(layout: SwipeRefreshLayout) {
        refreshLayout = layout
        refreshLayout?.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2,
                R.color.refresh_color_3)
        refreshLayout?.setOnRefreshListener {
            onRefreshed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
}