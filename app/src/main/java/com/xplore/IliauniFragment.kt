package com.xplore

import android.app.Fragment
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.xplore.reserve.ReserveInfoActivity
import kotlinx.android.synthetic.main.iliauni_library.*
import kotlinx.android.synthetic.main.stand_card.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by Nika on 9/25/2017.
 *
 * This class is only temporary; meant for the Iliauni Science Picnic
 *
 */

class IliauniFragment : Fragment() {

    private val TAG = "iliauniFrag"

    private val database: IliauniDatabaseHelper by lazy {
        IliauniDatabaseHelper(activity)
    }

    private val stands = ArrayList<Stand>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.iliauni_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        standsRecyclerView.layoutManager = LinearLayoutManager(activity);

        // Loading stand info from db
        database.openDataBase()
        doAsync {
            stands.addAll(database.getAllStands())

            database.close()
            uiThread {
                standsRecyclerView.adapter = StandRecyclerViewAdapter()
            }
        }

        /*Log.i(TAG, "stands:")
        database.openDataBase()
        for (stand in stands) {
            Log.i(TAG, "name = ${stand.name}")
            Log.i(TAG, "desc = ${stand.description}")
            Log.i(TAG, "lat = ${stand.latitude}")
            Log.i(TAG, "lng = ${stand.longitude}")
        }
        database.close()*/
    }

    private inner class StandRecyclerViewAdapter() : RecyclerView.Adapter<StandRecyclerViewAdapter.StandViewHolder>() {

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
            val ( _, name, _, image, _, _) = stands[position]

            if (name.length > 1) {
                holder.standName.visibility = View.VISIBLE
                holder.standName.text = name
            }

            holder.standImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))

            holder.itemView.setOnClickListener {
                //activity.startActivity(ReserveInfoActivity.getStartIntent(activity, results[position].id))
            }
        }

        override fun getItemCount(): Int = stands.size
    }

}