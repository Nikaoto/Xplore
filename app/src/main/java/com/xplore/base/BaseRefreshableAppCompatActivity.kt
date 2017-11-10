package com.xplore.base

/*
 * Created by Nikaoto on 9/6/2017.
 *
 * AppCompatActivity that refreshes when resumed
 *
 */

abstract class BaseRefreshableAppCompatActivity : BaseAppCompatActivity() {

    open var allowRefresh = false

    override fun onResume() {
        if (allowRefresh) {
            allowRefresh = false
            refresh()
        } else {
            allowRefresh = true
        }
        super.onResume()
    }

    open fun refresh() {
        val intent = intent
        finish()
        startActivity(intent)
    }
}