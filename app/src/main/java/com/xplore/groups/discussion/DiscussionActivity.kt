package com.xplore.groups.discussion

import android.app.Activity
import android.os.Bundle

/**
 * Created by Nikaoto on 8/11/2017.
 * TODO write description of this class - what it does and why.
 */
class DiscussionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView()
        startListeningForMessages()
    }

    private fun startListeningForMessages() {

    }

    private fun stopListeningForMessages() {

    }

    override fun onStop() {
        super.onStop()
        stopListeningForMessages()
    }
}