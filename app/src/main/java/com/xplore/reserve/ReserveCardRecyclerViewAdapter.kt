package com.xplore.reserve

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.xplore.General
import com.xplore.R
import kotlinx.android.synthetic.main.reserve_card.view.*
import java.util.*

/**
 * Created by Nikaoto on 6/16/2017.
 */

class ReserveCardRecyclerViewAdapter(val results: List<ReserveCard>,
                                     val activity: Activity,
                                     val icons: ArrayList<Int>)
    : RecyclerView.Adapter<ReserveCardRecyclerViewAdapter.ResultViewHolder>() {

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
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
            activity.startActivity(ReserveInfoActivity.getStartIntent(activity, results[position].id))
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = results.size
}