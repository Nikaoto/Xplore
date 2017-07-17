package com.xplore.groups.my

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.xplore.R

/**
 * Created by Nika on 7/17/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი იხსნება როდესაც მომხმარებელი არ არის გუნდებში გაწევრიანებული და ხსნის 'My Groups'
 * მენიუს.
 *
 * Description:
 * This fragment opens up when the user isn't currently in any group and opens 'My Groups'.
 *
 */

class EmptyGroupsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
        = inflater.inflate(R.layout.my_groups_empty, container, false)

}
