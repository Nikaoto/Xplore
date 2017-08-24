package com.xplore.reserve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewCompat
import android.widget.TextView
import com.xplore.database.DBManager
import com.xplore.R
import com.xplore.maps.MapActivity

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

    private fun TextView.safeSetText(txt: String?) {
        if (txt != null && txt.isNotEmpty()) {
            this.text = txt
        } else {
            this.setText(R.string.info_will_be_added)
        }
    }

    private fun setupLayout(reserve: Reserve) {
        collapsingToolbar.title = reserve.name
        reserveImageView.setImageResource(reserve.imageId)
        reserveIconFAB.setImageResource(Icons.black[reserve.iconId])
        descriptionTextView.safeSetText(reserve.description)
        faunaTextView.safeSetText(reserve.fauna)
        floraTextView.safeSetText(reserve.flora)
        equipmentTextView.safeSetText(reserve.equipment)
        tagsTextView.safeSetText(reserve.extratags)
        difficultyRatingBar.rating = reserve.difficulty.toFloat()

        showonmapButton.setOnClickListener() {
            mActivity.startActivity(
                    MapActivity.getStartIntent(
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
