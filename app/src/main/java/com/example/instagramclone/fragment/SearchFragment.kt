package com.example.instagramclone.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.activity.FriendProfileActivity
import com.example.instagramclone.adapter.SearchAdapter
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.helper.RecyclerItemClickListener
import com.example.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var usersList: ArrayList<User>
    private lateinit var usersRef: DatabaseReference

    private lateinit var searchAdapter: SearchAdapter

    private val loggedUser = FirebaseUser.getLoggedUserData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.searchViewSearch)
        recyclerView = view.findViewById(R.id.recyclerViewSearch)
        progressBar = view.findViewById(R.id.progressBarSearchUser)
        progressBar.visibility = View.GONE

        // initial configurations
        usersList = ArrayList()
        usersRef = FirebaseConfig.database.child("users")

        // config recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        searchAdapter = SearchAdapter(requireActivity(), usersList)
        recyclerView.adapter = searchAdapter

        // recycler view click event
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                requireActivity(),
                recyclerView,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val user = usersList[position]
                        val intent = Intent(activity, FriendProfileActivity::class.java)
                        val userJsonData = Json.encodeToString(user)
                        intent.putExtra(FriendProfileActivity.SELECTED_USER, userJsonData)
                        startActivity(intent)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        val user = usersList[position]
                        Toast.makeText(
                            activity,
                            "${user.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        )

        // config searchView
        searchView.queryHint = "Search users"
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //val searchText = newText?.uppercase()
                searchUsers(newText!!)
                return true
            }

        })

        return view
    }

    private fun searchUsers(searchText: String) {

        // clear users list
        usersList.clear()

        if (searchText.isNotEmpty()) {

            progressBar.visibility = View.VISIBLE

            val query = usersRef.orderByChild("name")
                .startAt(searchText)
                .endAt("$searchText\uf8ff")
            query.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // clear users list
                    usersList.clear()

                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if(user != null) {
                            if (loggedUser.id != user.id) usersList.add(user)
                        }
                    }

                    searchAdapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE

                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }

    }
}