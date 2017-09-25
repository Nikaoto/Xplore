package com.xplore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xplore.base.SearchFragment

/**
 * Created by Nika on 9/25/2017.
 * TODO write description of this class - what it does and why.
 */

class IliauniFragment : SearchFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            = inflater.inflate(R.layout.iliauni_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}