package com.xplore

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Nika on 11/9/2016.
 */

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.about_layout, container, false)
}