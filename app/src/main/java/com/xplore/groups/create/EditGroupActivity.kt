package com.xplore.groups.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.xplore.R
import com.xplore.TimeManager

/**
 * Created by Nikaoto on 8/13/2017.
 * TODO write description of this class - what it does and why.
 */
class EditGroupActivity : Activity() {

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String): Intent {
            return Intent(context, EditGroupActivity::class.java)
                    .putExtra("groupId", groupId)
        }
    }

    init {
        TimeManager.refreshGlobalTimeStamp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        setTitle(R.string.edit_group)
    }
}