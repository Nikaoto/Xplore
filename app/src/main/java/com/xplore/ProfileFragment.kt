package com.xplore

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.Auth

import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

import com.xplore.General.*
import kotlinx.android.synthetic.main.user_profile.*

/**
* Created by Nikaoto on 11/9/2016.
* TODO write description of this class - what it does and why.
*/

class ProfileFragment : Fragment() {

    private val googleApiClient: GoogleApiClient
            by lazy { ApiManager.getGoogleAuthApiClient(activity) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.user_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Start image loading animation
        imageProgressBar.visibility = View.VISIBLE

        logOutButton.setOnClickListener{ logOut() }
    }

    //TODO add Account field to Settings and LogOut() from there
    fun logOut() {
        if(accountStatus == LOGGED_IN){
            FirebaseAuth.getInstance().signOut()

            //TODO find which service was used to sign in and sign out accordingly
            //If (signed in with Google)
            Auth.GoogleSignInApi.signOut(googleApiClient)
            //else if (signed in with Facebook) ...

            currentUserId = ""
            accountStatus = NOT_LOGGED_IN

            Toast.makeText(activity, "Logged Out", Toast.LENGTH_SHORT).show() //TODO string resources

            //Refreshing current fragment
            fragmentManager.beginTransaction()
                    .detach(this@ProfileFragment)
                    .attach(this@ProfileFragment)
                    .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onResume() {
        super.onResume()

        if (!isNetConnected(activity)) {
            createNetErrorDialog(activity)
        } else if (!isUserSignedIn()) {
            imageProgressBar.visibility = View.INVISIBLE
            popSignInMenu(0.8, 0.6, view, activity)
        } else {
            showUserInfo()
        }
    }

    //Gets the user info from Firebase and loads them into views
    private fun showUserInfo() {
        refreshAccountStatus()
        val DBref = FirebaseDatabase.getInstance().reference
        val query = DBref.child("users").orderByKey().equalTo(currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && activity != null) {
                    var tempUser = User()
                    tempUser = dataSnapshot.children.iterator().next().getValue(User::class.java)
                    //Loading Image
                    Picasso.with(activity)
                            .load(tempUser.getProfile_picture_url())
                            .transform(RoundedCornersTransformation(
                                    resources.getInteger(R.integer.pic_big_angle),
                                    resources.getInteger(R.integer.pic_big_margin)))
                            .into(profileImageView)

                    //Diplaying user information on TextViews TODO after converting User class to kotlin, remove getters
                    nameTextView.text = "${tempUser.getFname()} ${tempUser.getLname()}"
                    repTextView.text = tempUser.getReputation().toString()
                    birthDateTextView.text =
                            "${getString(R.string.birth_date)}: ${General.putSlashesInDate(tempUser.getBirth_date())}"
                    telTextView.text = "${getString(R.string.tel)}: ${tempUser.getTel_num()}"
                    emailTextView.text = tempUser.getEmail()

                    //Stopping loading animation
                    imageProgressBar.visibility = View.INVISIBLE

                    /*TODO maybe add age?
                    val finalTempUser = tempUser
                    //Update current server time
                    val dateValue = HashMap<String, Any>()
                    dateValue.put("timestamp", ServerValue.TIMESTAMP)
                    DBref.child("date").setValue(dateValue)

                    DBref.child("date").child("timestamp").addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    tempTimeStamp = dataSnapshot.getValue(Long::class.java)
                                }
                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                    */
                    if (accountStatus != NOT_LOGGED_IN)
                        logOutButton.isEnabled = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })
    }

    //Checks and refreshes the account status
    private fun refreshAccountStatus() { //TODO add this in General
        if (FirebaseAuth.getInstance().currentUser != null)
            accountStatus = LOGGED_IN
        else
            accountStatus = NOT_LOGGED_IN
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }
}
