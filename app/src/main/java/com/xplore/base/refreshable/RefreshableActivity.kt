package com.xplore.base.refreshable

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.MenuItem
import com.xplore.R
import com.xplore.base.BaseAppCompatActivity

/**
 * Created by Nika on 11/12/2017.
 * TODO write description of this class - what it does and why.
 */

abstract class RefreshableActivity : BaseAppCompatActivity(), Refreshable {

    private var refreshLayout: SwipeRefreshLayout? = null

    override fun initRefreshLayout(layout: SwipeRefreshLayout) {
        refreshLayout = layout
        refreshLayout?.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2,
                R.color.refresh_color_3)
        refreshLayout?.setOnRefreshListener {
            onRefreshed()
        }
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