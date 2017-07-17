package com.xplore.groups.my

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xplore.R

/**
 * Created by Nika on 7/14/2017.
 * TODO write description of this class - what it does and why.
 */

class MyGroupsFragment() : Fragment() {

    val groupIds = ArrayList<String>()

    constructor(groupIds: ArrayList<String>) : this() {
        this.groupIds.addAll(groupIds)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.my_groups, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Toast.makeText(activity, "TODO: Load my groups", Toast.LENGTH_SHORT).show()
    }
}