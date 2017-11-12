package com.xplore.base.refreshable

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import com.xplore.R

/*
 * Created by Nika on 11/11/2017.
 */

abstract class RefreshableSearchFragment : RefreshableFragment(),
        SearchView.OnQueryTextListener {

    // TODO create FilterSearchFragment (for filters and stuff) and inherit this; override onCreateOptionsMenu

    private var searchView: SearchView? = null
    open var currentQuery: String = ""

    override var shouldRefreshOnResume: Boolean = false
    private var allowRefresh = false

    fun isCurrentQueryEmpty(): Boolean = currentQuery.trim().isEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // Sets onClickListener to given SearchView's TextView (recursively runs children down)
    private fun setSearchViewTextOnClickListener(v: View, listener: View.OnClickListener) {
        val group = v as ViewGroup
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            if (child is LinearLayout || child is RelativeLayout) {
                setSearchViewTextOnClickListener(child, listener)
            }

            if (child is TextView) {
                child.setOnClickListener(listener)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_refresh, menu)

        val searchItem = menu.findItem(R.id.action_search) as MenuItem
        searchView = searchItem.actionView as SearchView
        searchView?.let {
            it.setOnQueryTextListener(this)
            it.setOnSearchClickListener { v -> onSearchClick(v) }
            setSearchViewTextOnClickListener(it,
                    View.OnClickListener { v -> onSearchTextViewClick(v) })
            it.setOnCloseListener { onSearchViewClose(); false}
        }

        setUpSearchView(searchView)
        super.onCreateOptionsMenu(menu, inflater)
    }

    open fun onSearchClick(v: View?) {
        // Called when search icon clicked
    }

    open fun onSearchTextViewClick(v: View?) {
        // Called when the TextView inside the SearchView is clicked
    }

    open fun onSearchViewClose() {
        // Called when SearchView is closed by the little 'X'
    }

    open fun setUpSearchView(newSearchView: SearchView?) {
        // Here we set up the SearchView and customize it
        newSearchView?.queryHint = resources.getString(R.string.search_hint)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == null || newText.trim().isEmpty()) {
            onReset()
        }
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query == null || query.trim().isEmpty()) {
            return false
        } else {
            currentQuery = query
            return onSearch(query)
        }
    }

    open fun onSearch(query: String): Boolean {
        // Here we write searching logic
        return false
    }

    open fun onReset(): Boolean {
        // This is called when the query text is cleared/deleted
        return false
    }

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