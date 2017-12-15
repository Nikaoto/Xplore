package com.xplore.event

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.R
import com.xplore.util.FirebaseUtil
import kotlinx.android.synthetic.main.iliauni_library.*
import kotlinx.android.synthetic.main.stand_card.view.*


/**
 * Created by Nika on 9/25/2017.
 *
 * This class is only temporary; meant for the Iliauni Science Picnic
 *
 */

class IliauniFragment : Fragment() {

    private val TAG = "iliauniFrag"

    private val stands = ArrayList<Stand>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open_map) {
            activity.startActivity(Intent(activity, EventMapActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.iliauni_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        standsRecyclerView.layoutManager = LinearLayoutManager(activity);
        standsRecyclerView.adapter = StandRecyclerViewAdapter()

        // Loading stand info from db
/*        FirebaseUtil.getOrderedStandsRef().addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null) {
                            for (standSnapshot in dataSnapshot.children) {
                                val stand = standSnapshot.getValue(Stand::class.java)
                                if (stand != null) {
                                    stand.id = standSnapshot.key
                                    stands.add(stand)

                                    if (standsRecyclerView != null) {
                                        standsRecyclerView.adapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }

            override fun onCancelled(p0: DatabaseError?) { }
        })*/
    }

    private inner class StandRecyclerViewAdapter
        : RecyclerView.Adapter<StandRecyclerViewAdapter.StandViewHolder>() {

        inner class StandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal val standName: TextView = itemView.standTextView
            internal val standImage: ImageView = itemView.standImageView
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StandViewHolder {
            return StandViewHolder(
                    LayoutInflater.from(activity)
                            .inflate(R.layout.stand_card, parent, false)
            )
        }

        override fun onBindViewHolder(holder: StandViewHolder, position: Int) {
            // Destructure current stand for loading data (more readably)
            val (name, showTitle, _, image_url, _, _, _) = stands[position]

            // Image
            if (image_url.isNotEmpty()) {
                Picasso.with(activity)
                        .load(image_url)
                        .into(holder.standImage)
            } else {
                holder.standImage.visibility = View.GONE
            }

            // Title
            if (showTitle || image_url.isEmpty()) {
                holder.standName.visibility = View.VISIBLE
                holder.standName.text = name
            }

            holder.itemView.setOnClickListener {
                activity.startActivity(StandInfoActivity.getStartIntent(activity, name))
            }
        }

        override fun getItemCount(): Int = stands.size
    }
}