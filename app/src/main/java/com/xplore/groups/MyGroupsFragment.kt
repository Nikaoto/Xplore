package com.xplore.groups

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xplore.R

/**
 * Created by Nika on 7/14/2017.
 * TODO write description of this class - what it does and why.
 */

class MyGroupsFragment : Fragment() {

    var empty = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?): View {
        //TODO check for current joined groups (if empty)
        if (empty) {
            return inflater.inflate(R.layout.my_groups_empty, container, false)
        } else {
            return inflater.inflate(R.layout.my_groups, container, false)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!empty) {
            //TODO load current joined groups with GroupCardRecyclerViewAdapter
        }
    }
}