package com.xplore.groups.discussion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.discussion.*
import kotlinx.android.synthetic.main.message_list_item.view.*

/**
 * Created by Nikaoto on 8/11/2017.
 * TODO write description of this class - what it does and why.
 */

class DiscussionActivity : Activity() {

    //Firebase
    private val F_GROUPS = "groups"
    private val F_MEMBER_IDS = "member_ids"
    private val F_DISCUSSION = "discussion"
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var currentGroupRef: DatabaseReference
    private lateinit var discussionRef: DatabaseReference
    //
    private val  MESSAGE_LIMIT = 50

    private lateinit var groupId: String

    private val groupMembers = ArrayList<UserCard>()
    private val messageCards = ArrayList<MessageCard>()

    private var messageCount = 0
    private var initialMessageCount = 0

    private var listening = false
    private var memberCount = 0 //To find out when all members have been retrieved
    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String)
            = Intent(context, DiscussionActivity::class.java).putExtra("groupId", groupId)
    }

    private class MessageCard(val user_id: String = "",
                              val message: String = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discussion)

        groupId = intent.getStringExtra("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS/$groupId")
        discussionRef = currentGroupRef.child(F_DISCUSSION)

        checkIfDiscussionExists()

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = MessageListAdapter()

        //Gets all members from the group and stores them in groupMembers
        currentGroupRef.child(F_MEMBER_IDS).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null) {
                            memberCount = dataSnapshot.childrenCount.toInt()
                            Log.i("brejk", "memberCount=$memberCount")
                            for (memberId in dataSnapshot.children) {
                                getUserInfo(memberId.key)
                            }
                        } else {
                            printError()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                })

        initMessageCount()
    }

    private fun initMessageCount() {
        discussionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    initialMessageCount = dataSnapshot.childrenCount.toInt()
                    messageCount = initialMessageCount
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    //Gets info of an user with given Id
    private fun getUserInfo(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    val userCard = dataSnapshot.getValue(UserCard::class.java)
                    userCard?.let {
                        userCard.id = dataSnapshot.key
                        groupMembers.add(userCard)

                        memberCount--
                        if(memberCount == 0) {
                            startListeningForMessages()
                            enableMessaging()
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    //Checks if discussion node exists in firebase and creates it if not
    private fun checkIfDiscussionExists() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    if (!dataSnapshot.hasChild(F_DISCUSSION)) {
                        val newDiscussion = ArrayList<MessageCard>()
                        newDiscussion.add(MessageCard("1", "Feel free to start chatting :^)"))
                        discussionRef.setValue(newDiscussion)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    private fun printError() {
        Toast.makeText(this@DiscussionActivity, "Error retrieving group information",
                Toast.LENGTH_SHORT).show()
    }

    private val messageListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            if (dataSnapshot != null) {
                val message = dataSnapshot.getValue(MessageCard::class.java)
                if (message != null) {
                    val key = dataSnapshot.key.toInt()
                    messageCards.add(key, message)
                    messagesRecyclerView.adapter.notifyItemInserted(key)
                    messagesRecyclerView.adapter.notifyItemRangeChanged(key, messageCards.size)
                    messagesRecyclerView.scrollToPosition(key)

                    if (initialMessageCount == 0) {
                        messageCount++
                    } else {
                        initialMessageCount--
                    }
                }
            }
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildRemoved(p0: DataSnapshot?) {}

        override fun onCancelled(p0: DatabaseError?) {}
    }

    private fun startListeningForMessages() {
        if (!listening) {
            listening = true
            discussionRef.addChildEventListener(messageListener)
        }
    }

    private fun stopListeningForMessages() {
        if (listening) {
            listening = false
            discussionRef.removeEventListener(messageListener)
        }
    }

    //Adds a click listener to the send button; Call this only after message retrieval is finished.
    private fun enableMessaging() {
        sendMessageButton.setOnClickListener {
            sendMessage(MessageCard(General.currentUserId, sendMessageEditText.text.toString()))
            sendMessageEditText.setText("")
        }
    }

    private fun sendMessage(message: MessageCard) {
        if (message.message.isNotEmpty()) {
            //Add message with messageCount index
            discussionRef.child(messageCount.toString()).setValue(message)
        }
    }

    private inner class MessageListAdapter
        : RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

        private inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal val userImageView = itemView.userImageView
            internal val userFullName = itemView.userFullNameTextView
            internal val message = itemView.messageTextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = MessageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_item, parent, false))

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val currentCard = messageCards[position]
            val currentUser = getMemberById(currentCard.user_id)

            currentUser?.let {
                //Profile pic
                Picasso.with(this@DiscussionActivity)
                        .load(currentUser.profile_picture_url)
                        .transform(ImageUtil.tinyCircle(this@DiscussionActivity))
                        .into(holder.userImageView)
                holder.userImageView.setOnClickListener {
                    General.openUserProfile(this@DiscussionActivity, currentUser.id)
                }


                //Full name
                holder.userFullName.text = currentUser.getFullName()

                //Message
                holder.message.text = currentCard.message
            }
        }

        override fun getItemCount() = messageCards.size
    }

    private fun getMemberById(memberId: String): UserCard? {
        for(member in groupMembers) {
            if (member.id == memberId)
                return member
        }
        return null
    }

    override fun onStop() {
        super.onStop()
        stopListeningForMessages()
    }
}