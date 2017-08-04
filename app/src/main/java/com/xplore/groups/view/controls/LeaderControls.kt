package com.xplore.groups.view.controls

import android.app.AlertDialog
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xplore.R

import kotlinx.android.synthetic.main.leader_controls.*

/**
 * Created by Nikaoto on 8/4/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი ჩნდება ჯგუფის ქვეშ კართში და აძლებს ჯგუფის ლოიდერს გჯუფთან დაკავშირებულ
 * კონტროლებს.
 *
 * Description:
 * This is a fragment that shows at the bottom of a group to provide group related controls to the
 * leader.
 *
 */

class LeaderControls : Fragment() {

    private lateinit var groupId: String

    //TODO add discussion
    //TODO add remove members button
    //TODO add invite members button

    companion object {
        @JvmStatic
        fun newInstance(groupId: String): LeaderControls {
            val fragment = LeaderControls()
            val args = Bundle()
            args.putString("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.leader_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments.getString("groupId")

        editGroupButton.setOnClickListener {
            //startEditingGroup()
        }

        deleteGroupButton.setOnClickListener {
            confirmGroupDeletion()
        }
    }

    private fun confirmGroupDeletion() {
        val builder = AlertDialog.Builder(activity)
        //TODO string resources
        builder.setTitle(R.string.delete_group)
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton(R.string.yes, { _, _ -> deleteGroup() })
                .setNegativeButton(R.string.no, null)
        builder.show()
    }

    private fun deleteGroup() {

    }
}