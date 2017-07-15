package com.xplore.reserve

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.xplore.General
import com.xplore.R
import com.xplore.database.DBManager

import java.util.ArrayList

import kotlinx.android.synthetic.main.search_layout2.resultsRV
import kotlinx.android.synthetic.main.search_layout2.searchEditText
import kotlinx.android.synthetic.main.search_layout2.progressBar

/**
 * Created by Nika on 11/9/2016.
 */

class LibraryFragment : Fragment(), TextView.OnEditorActionListener {

    private val dbManager: DBManager by lazy { DBManager(activity) }

    private val answerCards = ArrayList<ReserveCard>()
    private var reserveCards = ArrayList<ReserveCard>()
    private var resultIDs: List<Int> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
         = inflater.inflate(R.layout.search_layout2, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        firstDisplayData()
    }

    fun init() {
        //Starting the loading animation
        progressBar.visibility = View.VISIBLE

        //setting up searchbar
        searchEditText.setSingleLine(true)
        searchEditText.setHint(R.string.search_tags_hint)
        searchEditText.setSelectAllOnFocus(true)
        searchEditText.setOnEditorActionListener(this)

        //Clear answer list
        answerCards.clear()

        //Setting layoutmanager
        resultsRV.layoutManager = LinearLayoutManager(activity)
    }

    fun firstDisplayData()
    {
        dbManager.openDataBase()

        //Load all reserveCards in a separate thread
          Thread(Runnable {
              //Loading data
              reserveCards = dbManager.getAllReserveCards()
              answerCards.addAll(reserveCards)
              //Creating & setting adapter
              val adapter = ReserveCardRecyclerViewAdapter(answerCards, activity, Icons.grey)
              resultsRV.post { resultsRV.adapter = adapter }
              progressBar.post { progressBar.visibility = View.INVISIBLE }
        }).start()
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
    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent?): Boolean {
        searchListItems(textView.text.toString().toLowerCase(), dbManager)
        return false
    }

    //Searches DB for query
    private fun searchListItems(query: String, dbManager: DBManager) {
        //Resetting answers
        answerCards.clear()

        //Searching Database
        resultIDs = dbManager.getIdFromQuery(query, General.DB_TABLE)

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
    }
}
