package com.explorify.xplore.xplore

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_reserve.view.*
import java.util.ArrayList

/**
 * Created by Nika on 6/16/2017.
 */

class ReserveCardRecyclerViewAdapter(val results: List<ReserveCard>, val activity: Activity, val icons: ArrayList<Int>)
    : RecyclerView.Adapter<ReserveCardRecyclerViewAdapter.ResultViewHolder>() {

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
            General.openReserveInfoFragment(results[position].id, activity)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = results.size
}