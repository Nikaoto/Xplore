package com.xplore.reserve

import android.app.Activity
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.xplore.R
import com.xplore.base.SearchFragment
import com.xplore.database.DBManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


/**
 * Created by Nikaoto on 11/9/2016.
 */

class LibraryFragment : SearchFragment() {

    //TODO add description and revamp companion obj- add newInstance

    companion object {
        const val REQ_CODE_RESERVE_INFO = 1
        const val ARG_GROUP_SEARCH_DESTINATION_ID = "search_dest_id"
    }

    private lateinit var act: Activity

    private val dbManager: DBManager by lazy { DBManager(activity) }

    private val answerCards = ArrayList<ReserveCard>()

    private lateinit var resultsRV: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.search_layout_nofab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultsRV = view.findViewById<RecyclerView>(R.id.resultsRV)

        init()
        firstDisplayData()
    }

    fun init() {
        // Clear answer list
        answerCards.clear()

        // Setting layoutmanager
        resultsRV.layoutManager = LinearLayoutManager(activity)
    }

    fun firstDisplayData() {
        dbManager.openDataBase()

        act = activity

        // Load all reserveCards in a separate thread
        doAsync {
            answerCards.addAll(dbManager.getAllReserveCards())

            uiThread {
                resultsRV.adapter = ReserveCardRecyclerViewAdapter(answerCards, act, Icons.grey)
            }
        }
    }

    override fun setUpSearchView(newSearchView: SearchView?) {
        newSearchView?.queryHint = resources.getString(R.string.search_tags_hint)
    }

    override fun onSearch(query: String): Boolean {
        searchListItems(query, dbManager)
        return super.onSearch(query)
    }

    override fun onReset(): Boolean {
        if (resultsRV.adapter.itemCount < dbManager.rowCount) {
            init()
            firstDisplayData()
        }
        return super.onReset()
    }

    //Searches DB for query
    private fun searchListItems(query: String, dbManager: DBManager) {
        // Reset answers
        answerCards.clear()

        // Get Ids of reserves
        val resultIds = dbManager.getIdsFromQuery(query)

        // Return results
        if (resultIds.isEmpty()) {
            Toast.makeText(activity, R.string.search_no_results, Toast.LENGTH_SHORT).show()
        } else {
            resultIds.mapTo(answerCards) { dbManager.getReserveCard(it) }
        }
        //Displaying the changes/results
        resultsRV.adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbManager.close()
    }
}
