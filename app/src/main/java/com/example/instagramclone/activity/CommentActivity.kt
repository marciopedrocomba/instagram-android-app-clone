package com.example.instagramclone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapter.CommentAdapter
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.Comment
import com.example.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class CommentActivity : AppCompatActivity() {

    companion object {
        const val ID = "post_id"
    }

    private lateinit var editTextComment: EditText
    private lateinit var imageViewSendComment: ImageView
    private lateinit var recyclerViewComments: RecyclerView

    private lateinit var postId: String
    private lateinit var user: User

    private lateinit var commentAdapter: CommentAdapter
    private val commentsList = ArrayList<Comment>()

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var commentsRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        //init components
        editTextComment = findViewById(R.id.editTextComment)
        imageViewSendComment = findViewById(R.id.imageViewSendComment)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)

        // initial configurations
        user = FirebaseUser.getLoggedUserData()
        firebaseRef = FirebaseConfig.database

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Comments"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        // config recycler view
        commentAdapter = CommentAdapter(applicationContext, commentsList)
        recyclerViewComments.setHasFixedSize(true)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.adapter = commentAdapter

        // get the selected post comment id
        postId = intent?.extras?.getString(ID).toString()

        // add save comment button click event
        imageViewSendComment.setOnClickListener {
            saveComment()
        }

    }

    override fun onStart() {
        super.onStart()
        getComments()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListener != null) commentsRef.removeEventListener(valueEventListener!!)
    }

    private fun getComments() {
        commentsRef = firebaseRef.child("comments")
            .child(postId)
        valueEventListener = commentsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentsList.clear()

                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    if (comment != null) commentsList.add(comment)
                }

                commentAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun saveComment() {
        val commentText: String = editTextComment.text.toString()
        if (commentText.isEmpty()) {

            Toast.makeText(
                this,
                "fill the comment field",
                Toast.LENGTH_SHORT
            ).show()

        }else {

            val comment = Comment()
            comment.postId = postId
            comment.userId = user.id
            comment.username = user.name
            comment.userPhoto = user.photo
            comment.comment = commentText
            if (comment.save()) {
                editTextComment.setText("")
                Toast.makeText(
                    this,
                    "Comment saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

}