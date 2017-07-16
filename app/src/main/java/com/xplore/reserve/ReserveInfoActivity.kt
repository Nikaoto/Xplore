package com.xplore.reserve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewCompat
import com.xplore.database.DBManager
import com.xplore.R
import com.xplore.maps.MapsActivity

import kotlinx.android.synthetic.main.reserve_info.*

/**
* Created by Nikaoto on 11/16/2016.
*
* აღწერა:
* ეს მარტივი ფრაგმენტი ანახებს ნაკრძალის ინფორმაციას
*
* Description:
* This is a simple fragment that shows info about a certain reserve
*
*/

class ReserveInfoActivity() : Activity(), AppBarLayout.OnOffsetChangedListener {

    private val IMAGE_SHOW_PERCENT = 45
    private val mActivity = this
    private var maxScrollSize = 0
    private var isImageHidden = false

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, reserveId: Int)
            = Intent(context, ReserveInfoActivity::class.java).putExtra("chosen_element", reserveId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserve_info)

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
    }

    fun setupLayout(reserve: Reserve) {
        collapsingToolbar.setTitle(reserve.name)
        groupImageView.setImageResource(reserve.imageId)
        reserveIconFAB.setImageResource(Icons.black[reserve.iconId])
        descriptionTextView.text = reserve.description
        faunaTextView.text = reserve.fauna
        floraTextView.text = reserve.flora
        equipmentTextView.text = reserve.equipment
        tagsTextView.text = reserve.extratags
        difficultyRatingBar.rating = reserve.difficulty.toFloat()

        showonmapButton.setOnClickListener() {
            mActivity.startActivity(
                    MapsActivity.getStartIntent(
                            mActivity,
                            true,
                            reserve.name,
                            reserve.location.latitude,
                            reserve.location.longitude
                    )
            )
        }
    }
}
