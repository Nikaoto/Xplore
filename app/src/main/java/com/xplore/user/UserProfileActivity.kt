package com.xplore.user

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.DateUtil
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import com.xplore.account.EditProfileActivity
import com.xplore.base.BaseRefreshableAppCompatActivity
import kotlinx.android.synthetic.main.user_profile.*

/**
 * Created by Nikaoto on 7/5/2017.
 *
 * აღწერა:
 * ეს მოქმედება აჩვენებს ნებისმიერი მომხმარებლის პროფილის გვერდს მისი ID-ს მიხედვით.
 * ინტენტიდან იღებს "userId" სტრინგს.
 *
 * Description:
 * This activity displays any user's profile page with the given ID.
 * Receives "userId" string from intent.
 *
 */

class UserProfileActivity : BaseRefreshableAppCompatActivity() {

    private val userId: String by lazy { getPassedUserId() }
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fetchUserInfo(userId)
    }

    private fun getPassedUserId(): String {
        val tempUserId = intent.getStringExtra("userId")
        if (tempUserId != null) {
            return tempUserId
        } else {
            return General.currentUserId
        }
    }

    private fun fetchUserInfo(userId: String){
        val query = usersRef.child(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val tempUser = dataSnapshot.getValue(User::class.java)
                    if (tempUser != null) {
                        tempUser.setId(dataSnapshot.key)
                        displayUserInfo(tempUser)

                        // If user viewing his/her profile
                        if (userId == General.currentUserId) {
                            configureEditProfileButton(tempUser)
                        }
                    } else { printError() }
                } else { printError() }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    private fun displayUserInfo(user: User){
        //Removing small image from cache
        Picasso.with(this).invalidate(user.getProfile_picture_url())
        //Loading profile picture
        Picasso.with(this)
                .load(user.getProfile_picture_url())
                .transform(ImageUtil.largeCircle(this))
                .placeholder(R.drawable.picasso_load_anim)
                .into(userImageView)

        fullNameTextView.text = "${user.getFname()} ${user.getLname()}"
        reputationCombinedTextView.text = user.getReputation().toString() +
                " " + resources.getString(R.string.reputation)
        birthDateTextView.text = DateUtil.putSlashesInDate(user.getBirth_date())
        telephoneTextView.text = user.getTel_num()
        emailTextView.text = user.getEmail()
    }

    private fun configureEditProfileButton(user: User) {
        editProfileButton.visibility = View.VISIBLE
        editProfileButton.setOnClickListener {
            startActivity(EditProfileActivity.getStartIntent(this@UserProfileActivity, user))
        }
    }

    fun printError() {
        Toast.makeText(this@UserProfileActivity, R.string.error, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}