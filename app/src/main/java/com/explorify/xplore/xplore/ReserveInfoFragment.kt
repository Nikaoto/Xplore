package com.explorify.xplore.xplore

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewCompat

import kotlinx.android.synthetic.main.reserve_info2.*

/**
 * Created by nikao on 11/16/2016.
 */

class ReserveInfoFragment() : FragmentActivity(), AppBarLayout.OnOffsetChangedListener {

    private val IMAGE_SHOW_PERCENT = 45
    private val mActivity = this
    private var maxScrollSize = 0
    private var isImageHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserve_info2)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        appBar.addOnOffsetChangedListener(this)
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

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if(maxScrollSize == 0)
            maxScrollSize = appBarLayout.totalScrollRange

        val currentScrollPercentage = (Math.abs(verticalOffset)) * 100 / maxScrollSize

        if(currentScrollPercentage >= IMAGE_SHOW_PERCENT)
            if(!isImageHidden){
                isImageHidden = true
                ViewCompat.animate(reserveIconFAB).scaleY(0f).scaleX(0f).start()
            }
        if(currentScrollPercentage < IMAGE_SHOW_PERCENT)
            if(isImageHidden){
                isImageHidden = false
                ViewCompat.animate(reserveIconFAB).scaleY(1f).scaleX(1f).start()
            }
    }

    override fun onResume() {
        super.onResume()
        if (MapFragment.MAPS_CLOSED) {
            fragmentManager.popBackStack()
        }
    }

    fun setupLayout(reserve: Reserve) {
        collapsingToolbar.setTitle(reserve.name)
        reserveImageView.setBackgroundResource(reserve.imageId)
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
