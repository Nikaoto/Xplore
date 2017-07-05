package com.xplore

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.user_profile.*

/**
 * Created by Nikaoto on 7/5/2017.
 *
 * აღწერა:
 * ეს მოქმედება აჩვენებს ნებისმიერი მომხმარებლის პროფილის გვერდს მისი ID-ს მიხედვით
 *
 * Description:
 * This activity displays any user's profile page with the given ID
 *
 */

class UserProfileActivity : Activity() {

    private val userId: String by lazy { intent.getStringExtra("userId") }
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        //Start load animation
        imageProgressBar.visibility = View.VISIBLE

        fetchUserInfo(userId)
    }

    private fun fetchUserInfo(userId: String){
        val query = usersRef.orderByKey().equalTo(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && this@UserProfileActivity != null) {
                    val tempUser = dataSnapshot.children.iterator().next().getValue(User::class.java) //TODO remove .java after converting User to kotlin
                    displayUserInfo(tempUser)
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
                .transform(RoundedCornersTransformation(
                        resources.getInteger(R.integer.pic_big_angle),
                        resources.getInteger(R.integer.pic_big_margin)))
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