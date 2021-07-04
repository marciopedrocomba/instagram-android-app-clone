package com.example.instagramclone.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapter.FeedAdapter
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.Feed
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class FeedFragment : Fragment() {

    private lateinit var recyclerViewFeed: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var feedAdapter: FeedAdapter
    private val feedsList: ArrayList<Feed> = ArrayList()
    private lateinit var valueEventListenerFeed: ValueEventListener
    private lateinit var feedRef: DatabaseReference
    private lateinit var loggedUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_feed, container, false)

        // initial configurations
        loggedUserId = FirebaseUser.getUserId()
        feedRef = FirebaseConfig.database
            .child("feed")
            .child(loggedUserId)

        // init components
        recyclerViewFeed = view.findViewById(R.id.recyclerViewFeed)
        progressBar = view.findViewById(R.id.progressBarLoadingFeed)
        progressBar.visibility = View.GONE

        feedAdapter = FeedAdapter(requireContext(), feedsList)
        recyclerViewFeed.setHasFixedSize(true)
        recyclerViewFeed.layoutManager = LinearLayoutManager(activity)
        recyclerViewFeed.adapter = feedAdapter

        return view
    }

    private fun getFeeds() {
        progressBar.visibility = View.VISIBLE
        valueEventListenerFeed = feedRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                feedsList.clear()
                for (data in snapshot.children) {
                    val feed = data.getValue(Feed::class.java)
                    if(feed != null) feedsList.add(feed)
                }
                feedsList.reverse()
                feedAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    override fun onStart() {
        super.onStart()
        getFeeds()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListenerFeed != null) feedRef.removeEventListener(valueEventListenerFeed)
    }

}