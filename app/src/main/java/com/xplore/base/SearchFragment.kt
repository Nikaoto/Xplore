package com.xplore.base

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import com.xplore.R

/**
 * Created by Nika on 9/4/2017.
 *
 * Base search fragment made for other fragment that uses a search to inherit
 *
 */

open class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        val searchItem = menu.findItem(R.id.action_search) as MenuItem
        searchView = searchItem.actionView as SearchView
        searchView?.setOnQueryTextListener(this)
        setUpSearchView(searchView)
        super.onCreateOptionsMenu(menu, inflater)
    }

    open fun setUpSearchView(newSearchView: SearchView?) {
        // Here we set up the searchview and customize it
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

    fun showProgressBar() {
        searchView?.let {
            val id = it.context.resources.getIdentifier("android:id/search_plate", null, null)
            val progressBar = it.findViewById(id).findViewById(R.id.searchProgressBar)
            if (progressBar != null) {
                progressBar.animate().setDuration(200).alpha(1f).start()
            } else {
                val view = LayoutInflater.from(activity).inflate(R.layout.loading_icon, null)
                (it.findViewById(id) as ViewGroup).addView(view, 1)
            }
        }
    }

    fun hideProgressBar() {
        searchView?.let {
            val id = it.context.resources.getIdentifier("android:id/search_plate", null, null)
            val progressBar = it.findViewById(id).findViewById(R.id.searchProgressBar)
            if (progressBar != null) {
                progressBar.animate().setDuration(200).alpha(0f).start()
            }
        }
    }
}