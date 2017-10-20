package com.xplore.reserve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.TextView
import com.xplore.General
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
* This is a simple fragment that shows info of a certain reserve
*
*/

class ReserveInfoActivity() : BaseActivity(), AppBarLayout.OnOffsetChangedListener {

    private val IMAGE_SHOW_PERCENT = 45
    private val mActivity = this
    private var maxScrollSize = 0
    private var isImageHidden = false

    companion object {
        private const val ARG_CHOSEN_ELEMENT = "chosen_element"

        @JvmStatic
        fun getStartIntent(context: Context, reserveId: Int): Intent
                = Intent(context, ReserveInfoActivity::class.java)
                    .putExtra(ARG_CHOSEN_ELEMENT, reserveId)
    }

    private val chosenReserve: Int by lazy {
        intent.getIntExtra(ARG_CHOSEN_ELEMENT, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserve_info)

        toolbar.setNavigationOnClickListener { onBackPressed() }
        appBar.addOnOffsetChangedListener(this)
        // Set up Layout according to info from chosen Reserve
        displayData(LoadReserve(chosenReserve))
    }

    // Loads reserve from database by Id and returns it
    private fun LoadReserve(resId: Int): Reserve {
        val dbManager = DBManager(this)
        dbManager.openDataBase()
        // Get reserve info from DB
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

        // Show On Map
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

        // Find Hikes With This Destination
        if (General.isUserSignedIn()) {
            findTripsButton.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra(LibraryFragment.ARG_GROUP_SEARCH_DESTINATION_ID,
                        chosenReserve)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        } else {// TODO make this pop signin dialog?
            findTripsButton.isEnabled = false
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed()
    }
}
