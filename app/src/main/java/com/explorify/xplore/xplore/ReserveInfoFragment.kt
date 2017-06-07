package com.explorify.xplore.xplore

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity

import kotlinx.android.synthetic.main.reserve_info.*

/**
 * Created by nikao on 11/16/2016.
 */

class ReserveInfoFragment() : FragmentActivity() {

    internal val mActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserve_info)

        //Sets up Layout acording to info from chosen Reserve
        val chosenReserve = intent.getIntExtra("chosen_element", 0)
        setupLayout(LoadReserve(chosenReserve))
    }

    //Loads reserve from database by Id and returns it
    private fun LoadReserve(resId: Int): Reserve {
        val dbManager = DBManager(this)
        dbManager.openDataBase()
        //Getting reserve info from DB
        return dbManager.getReserve(resId)
    }

    override fun onResume() {
        super.onResume()
        if (MapFragment.MAPS_CLOSED) {
            fragmentManager.popBackStack()
        }
    }

    fun setupLayout(reserve: Reserve) {
        headerButton.setBackgroundResource(reserve.imageId)
        headerButton.text = reserve.name
        descriptionTextView.text = reserve.description
        faunaTextView.text = reserve.fauna
        floraTextView.text = reserve.flora
        equipmentTextView.text = reserve.equipment
        tagsTextView.text = reserve.extratags
        difficultyRatingBar.rating = reserve.difficulty.toFloat()

        showonmapButton.setOnClickListener() {
            MapFragment.MAPS_CLOSED = false
            val intent = Intent(mActivity, MapsActivity::class.java)
            intent.putExtra("show_reserve", true)
            intent.putExtra("reserve_name", reserve.name)
            intent.putExtra("reserve_latitude", reserve.location.latitude)
            intent.putExtra("reserve_longitude", reserve.location.longitude)
            mActivity.startActivity(intent)
        }
    }
}
