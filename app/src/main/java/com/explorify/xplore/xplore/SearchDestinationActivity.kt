package com.explorify.xplore.xplore

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_reserve.view.*
import kotlinx.android.synthetic.main.search_layout2.*

import java.util.ArrayList

/**
 * Created by nikao on 2/23/2017.
 */


class SearchDestinationActivity : Activity() {

    private val dbManager: DBManager by lazy { DBManager(this) }

    companion object {
        private val CHOSEN_DEST_DEFAULT_VAL = -1
        private var chosenDestId = CHOSEN_DEST_DEFAULT_VAL
    }

    private var reserveCards = ArrayList<ReserveCard>()
    private val answerCards = ArrayList<ReserveCard>()
    private val iconList = arrayListOf<Int>(
            R.drawable.ic_tent_grey600_36dp,
            R.drawable.ic_nature_people_grey600_36dp,
            R.drawable.ic_castle_grey600_36dp,
            R.drawable.ic_paw_grey600_36dp
    )

    private var resultIDs: List<Int> = ArrayList()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout2)

        init()
        firstDisplayData()
    }

    fun init(){
        //Starting the loading animation
        progressBar.visibility = View.VISIBLE

        //setting up searchbar
        searchEditText.setSingleLine(true)
        searchEditText.setHint(R.string.search_name_hint)
        searchEditText.setSelectAllOnFocus(true)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //Log.println(Log.INFO, "brejk", "${s.toString()} is the search query")
                searchListItems(s.toString().toLowerCase())
            }
        })

        //Clear answer list
        answerCards.clear()

        //Setting layoutmanager
        resultsRV.layoutManager = LinearLayoutManager(this)
    }

    fun firstDisplayData() {
        dbManager.openDataBase()

        //Load all reserveCards in a separate thread
        Thread(Runnable {
            //Loading data
            reserveCards = dbManager.getAllReserveCards()
            answerCards.addAll(reserveCards)
            //Creating & setting adapter
            val adapter = RVAdapter(answerCards, this, iconList)
            resultsRV.post { resultsRV.adapter = adapter }
            progressBar.post { progressBar.visibility = View.INVISIBLE }
        }).start()
    }

    private fun searchListItems(query: String) {
        //Resetting answers
        answerCards.clear()

        //Searching Database
        resultIDs = dbManager.getIdFromName(query, General.DB_TABLE)

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

    class RVAdapter(val results: List<ReserveCard>, val activity: Activity, val icons: ArrayList<Int>)
        : RecyclerView.Adapter<RVAdapter.ResultViewHolder>(){

        class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            internal val reserveName: TextView
            internal val reserveImage: ImageView
            internal val reserveIcon: ImageView

            init{
                reserveName = itemView.reserveNameTextView
                reserveImage = itemView.reserveImageView
                reserveIcon = itemView.reserveIconImageView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder =
                ResultViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.list_item_reserve, parent, false)
                )

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.reserveName.setText(results[position].name)
            holder.reserveImage.setImageResource(results[position].imageId)
            holder.reserveIcon.setImageResource(icons[results[position].iconId])
            holder.itemView.setOnClickListener {
                General.HideKeyboard(activity)
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
                    .setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            chosenDestId = reserveCard.id
                            Log.println(Log.INFO, "brejk", "chosen dest id is $chosenDestId")
                            activity.onBackPressed()
                        }
                    })
                    .setNegativeButton(R.string.no, null);

            builder.show();
        }

        override fun getItemCount(): Int = results.size
    }

    override fun onBackPressed() {
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