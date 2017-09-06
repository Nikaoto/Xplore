package com.xplore.reserve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.TextView
import com.xplore.R
import com.xplore.base.BaseActivity
import com.xplore.database.DBManager
import com.xplore.maps.GroupMapActivity
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

class ReserveInfoActivity() : BaseActivity(), AppBarLayout.OnOffsetChangedListener {

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
        displayData(LoadReserve(chosenReserve))
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
            ((this.parent as View).parent as View).visibility = View.GONE
        }
    }

    private fun displayData(reserve: Reserve) {
        // Name
        collapsingToolbar.title = reserve.name

        // Image
        reserveImageView.setImageResource(reserve.imageId)

        // Type FAB
        reserveIconFAB.setImageResource(Icons.black[reserve.iconId])

        // Description
        if (reserve.description != null && reserve.description.isNotEmpty()) {
            descriptionTextView.safeSetText(reserve.description)
        } else {
            descriptionTextView.setText(R.string.info_will_be_added)
        }

        // Tags
        faunaTextView.safeSetText(reserve.fauna)
        floraTextView.safeSetText(reserve.flora)
        equipmentTextView.safeSetText(reserve.equipment)
        tagsTextView.safeSetText(reserve.extratags)

        // Difficulty
        if (reserve.difficulty == 0) {
            difficultyCardView.visibility = View.GONE
        } else {
            difficultyRatingBar.rating = reserve.difficulty.toFloat()
        }

        if (reserve.hasNoLocation()) {
            showonmapButton.visibility = View.GONE
        } else {
            showonmapButton.setOnClickListener {
                mActivity.startActivity(
                        GroupMapActivity.getStartIntent(
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
}
