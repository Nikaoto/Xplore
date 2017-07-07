package com.xplore.groups

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xplore.General
import com.xplore.General.accountStatus

import com.xplore.General.popSignInMenu
import com.xplore.R
import com.xplore.groups.create.CreateGroupFragment
import com.xplore.groups.search.SearchGroupsFragment
import kotlinx.android.synthetic.main.group_menu_layout.createPartyButton
import kotlinx.android.synthetic.main.group_menu_layout.joinPartyButton


/**
 * Created by Nikaoto on 11/9/2016.
 */

class GroupMenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.group_menu_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        joinPartyButton.setOnClickListener {
            if (General.isUserSignedIn()) {
                fragmentManager.beginTransaction().replace(R.id.fragment_container,
                        SearchGroupsFragment()).addToBackStack("4").commit()
            } else {
                popSignInMenu(0.8, 0.6, view, activity)
            }
        }

        createPartyButton.setOnClickListener {
            if (General.isUserSignedIn()) {
                fragmentManager.beginTransaction().replace(R.id.fragment_container,
                        CreateGroupFragment()).addToBackStack("4").commit()
            } else {
                popSignInMenu(0.8, 0.6, view, activity)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /*if(General.accountStatus == General.LOGGED_IN) {
            Toast.makeText(getActivity(), "Signed In", Toast.LENGTH_SHORT).show(); //TODO string resources
        }*/

        //TODO show this toast after exitting register activity
        if (General.accountStatus == General.JUST_REGISTERED) {
            Toast.makeText(activity, "Registered", Toast.LENGTH_SHORT).show() //TODO string resources
            accountStatus = General.LOGGED_IN
        }
    }
}
