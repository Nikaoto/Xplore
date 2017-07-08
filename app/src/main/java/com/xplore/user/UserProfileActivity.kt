package com.xplore.user

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.CircleTransformation
import com.xplore.General
import com.xplore.R
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

//TODO open registration when not signed in
class UserProfileActivity : Activity() {

    private val userId: String by lazy { getPassedUserId() }
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        //Start load animation
        imageProgressBar.visibility = View.VISIBLE
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
        val query = usersRef.orderByKey().equalTo(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && this@UserProfileActivity != null) {
                    val tempUser = dataSnapshot.children.iterator().next().getValue(User::class.java) //TODO remove .java after converting User to kotlin
                    displayUserInfo(tempUser!!)
                } else {
                    Toast.makeText(this@UserProfileActivity,
                            "User does not exist", Toast.LENGTH_SHORT).show()//TODO String resources
                }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    private fun displayUserInfo(user: User){
        //TODO remove get after converting User class to kotlin
        //Profile picture
        Picasso.with(this)
                .load(user.getProfile_picture_url())
                .transform(CircleTransformation(profileImageView.height, profileImageView.width))
                .into(profileImageView)

        nameTextView.text = "${user.getFname()} ${user.getLname()}"
        repTextView.text = user.getReputation().toString()
        birthDateTextView.text =
                "${getString(R.string.birth_date)}: ${General.putSlashesInDate(user.getBirth_date())}"
        telTextView.text = "${getString(R.string.tel)}: ${user.getTel_num()}"
        emailTextView.text = user.getEmail()

        //Stop loading animation
        imageProgressBar.visibility = View.INVISIBLE

    }
}