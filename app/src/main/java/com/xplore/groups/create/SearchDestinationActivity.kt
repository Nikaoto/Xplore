package com.xplore.groups.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.xplore.General
import com.xplore.R
import com.xplore.base.BaseAppCompatActivity
import com.xplore.database.DBManager
import com.xplore.reserve.Icons
import com.xplore.reserve.ReserveCard
import kotlinx.android.synthetic.main.reserve_card.view.*
import kotlinx.android.synthetic.main.search_layout2.*
import java.util.*

/**
* Created by Nikaoto on 2/23/2017.
* TODO write description of this class - what it does and why.
*/


class SearchDestinationActivity : BaseAppCompatActivity() {

    private val dbManager: DBManager by lazy { DBManager(this) }

    private val CHOSEN_DEST_DEFAULT_VAL = -1
    private var chosenDestId = CHOSEN_DEST_DEFAULT_VAL


    private var reserveCards = ArrayList<ReserveCard>()
    private val answerCards = ArrayList<ReserveCard>()

    private var resultIDs: List<Int> = ArrayList()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout2)
        setTitle(R.string.choose_destination)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        init()
        firstDisplayData()
    }

    private fun init(){
        //Starting the loading animation
        progressBar.visibility = View.GONE

        //setting up searchbar
        searchEditText.setSingleLine(true)
        searchEditText.setHint(R.string.search_name_hint)
        searchEditText.setSelectAllOnFocus(true)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchListItems(s.toString().toLowerCase())
            }
        })

        //Clear answer list
        answerCards.clear()

        //Setting layoutmanager
        resultsRV.layoutManager = LinearLayoutManager(this)
    }

    private fun firstDisplayData() {
        dbManager.openDataBase()

        resultsRV.adapter = RVAdapter(answerCards, this, Icons.grey)

        //Load all reserveCards in a separate thread
        Thread(Runnable {
            //Loading data
            reserveCards = dbManager.getAllReserveCards()
            answerCards.addAll(reserveCards)
            //answerCards.addAll(reserveCards)
            //Creating & setting adapter
            resultsRV.post { resultsRV.adapter.notifyDataSetChanged()}
        }).start()
    }

    private fun searchListItems(query: String) {
        //Resetting answers
        answerCards.clear()

        //Searching Database
        resultIDs = dbManager.getIdFromName(query, DBManager.DB_TABLE)

        //Returning Results
        if (!resultIDs.isEmpty()) {
            var index = 0
            for (result in resultIDs) {
                //result is the single ID of an answer
                answerCards.add(index, reserveCards[result])
                index++
            }
        }
        //Displaying the changes/results
        resultsRV.adapter.notifyDataSetChanged()
    }

    inner class RVAdapter(val results: List<ReserveCard>,
                          val activity: Activity,
                          val icons: ArrayList<Int>)
        : RecyclerView.Adapter<RVAdapter.ResultViewHolder>(){

        inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            internal val reserveName: TextView = itemView.reserveNameTextView
            internal val reserveImage: ImageView = itemView.reserveImageView
            internal val reserveIcon: ImageView = itemView.reserveIconImageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder =
                ResultViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.reserve_card, parent, false)
                )

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.reserveName.setText(results[position].name)
            holder.reserveImage.setImageResource(results[position].imageId)
            holder.reserveIcon.setImageResource(icons[results[position].iconId])
            holder.itemView.setOnClickListener {
                General.hideKeyboard(activity)
                showConfirmDialog(results[position])
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)
        }

        fun showConfirmDialog(reserveCard: ReserveCard) {
            //Building the alert dialog
            val builder = AlertDialog.Builder(activity);
            val message = "${activity.resources.getString(R.string.question_choose_reserve)} ${reserveCard.name}?"
            builder.setMessage(message)
                    .setTitle(reserveCard.name)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        chosenDestId = reserveCard.id
                        activity.onBackPressed()
                    }
                    .setNegativeButton(R.string.no, null);

            builder.show();
        }

        override fun getItemCount(): Int = results.size
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        General.hideKeyboard(this)
        val resultIntent = Intent()

        //Checking if destination was chosen
        if (chosenDestId == CHOSEN_DEST_DEFAULT_VAL) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            resultIntent.putExtra("chosen_destination_id", chosenDestId)
            setResult(Activity.RESULT_OK, resultIntent)
        }
        finish()
    }
}