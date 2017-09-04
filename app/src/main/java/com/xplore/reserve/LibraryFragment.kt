package com.xplore.reserve

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.xplore.R
import com.xplore.base.SearchFragment
import com.xplore.database.DBManager
import kotlinx.android.synthetic.main.search_layout3.*
import java.util.*


/**
 * Created by Nikaoto on 11/9/2016.
 */

class LibraryFragment : SearchFragment() {

    private val dbManager: DBManager by lazy { DBManager(activity) }

    private val answerCards = ArrayList<ReserveCard>()
    private var reserveCards = ArrayList<ReserveCard>()
    private var resultIDs: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.search_layout_nofab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        firstDisplayData()
    }

    fun init() {

        //Clear answer list
        answerCards.clear()


        //Setting layoutmanager
        resultsRV.layoutManager = LinearLayoutManager(activity)
    }

    fun firstDisplayData() {
        dbManager.openDataBase()

        //Load all reserveCards in a separate thread
        Thread().run {
            //Loading data
            reserveCards = dbManager.getAllReserveCards()
            answerCards.addAll(reserveCards)

            //Creating & setting adapter
            val adapter = ReserveCardRecyclerViewAdapter(answerCards, activity, Icons.grey)
            resultsRV.adapter = adapter
        }
    }

    override fun setUpSearchView(newSearchView: SearchView?) {
        newSearchView?.queryHint = resources.getString(R.string.search_tags_hint)
    }

    override fun onSearch(query: String): Boolean {
        showProgressBar()
        searchListItems(query, dbManager)
        return false
    }

    /*
            //TODO test after adding all reserves vs current data gathering method
            //Gets each ReserveCard separately. Takes longer but is a smoother process.
            private fun populateCardList(reserveCards: ArrayList<ReserveCard>, dbManager: DBManager) {
                val table = General.DB_TABLE
                reserveCards.clear()

                Thread(Runnable {
                    //Getting each resID separately
                    for (i in 0..MainActivity.RESERVE_NUM - 1) {
                        reserveCards.add(dbManager.getReserveCard(i))
                        resultsRV.post { resultsRV.adapter.notifyDataSetChanged() }
                    }
                }).start()
            }
       */

    //Searches DB for query
    private fun searchListItems(query: String, dbManager: DBManager) {
        //Resetting answers
        answerCards.clear()

        //Searching Database
        resultIDs = dbManager.getIdFromQuery(query, DBManager.DB_TABLE)

        //Returning Results
        if (resultIDs.isEmpty()) {
            Toast.makeText(activity, R.string.search_no_results, Toast.LENGTH_SHORT).show()

        } else {
            var index = 0
            for (result in resultIDs) {
                //result is the single ID of an answer
                answerCards.add(index, reserveCards[result])
                index++
            }
            //resultsRV.adapter = ReserveCardRecyclerViewAdapter(answerCards, activity, iconList)
        }
        //Displaying the changes/results
        resultsRV.adapter.notifyDataSetChanged()

        hideProgressBar()
    }
}
