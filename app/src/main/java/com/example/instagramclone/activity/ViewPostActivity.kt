package com.example.instagramclone.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.Feed
import com.example.instagramclone.model.LikedFeed
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.like.LikeButton
import com.like.OnLikeListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ViewPostActivity : AppCompatActivity() {

    companion object {
        const val SELECTED_POST = "selected_post"
        const val SELECTED_USER = "selected_user"
    }

    private lateinit var circularImageView: CircleImageView
    private lateinit var imageView: ImageView
    private lateinit var textViewPostProfileName: TextView
    private lateinit var textViewPostProfileLikes: TextView
    private lateinit var textViewPostProfileDescription: TextView
    private lateinit var likeButton: LikeButton
    private lateinit var commentImageView: ImageView

    private val loggedUser: User = FirebaseUser.getLoggedUserData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_post)

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Post"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        // initialize components
        initComponents()

        // get data from the activity that called  this
        val bundle: Bundle? = intent?.extras
        if (bundle != null) {
            val selectedPostJsonData = bundle.getString(SELECTED_POST)
            val selectedUserJsonData = bundle.getString(SELECTED_USER)

            val selectPost = Json.decodeFromString(selectedPostJsonData!!) as Post
            val selectedUser = Json.decodeFromString(selectedUserJsonData!!) as User

            // show user data
            if (selectedUser.photo == null) {
                circularImageView.setImageResource(R.drawable.avatar)
            }else {
                val uriProfileImage: Uri = Uri.parse(selectedUser.photo.toString())
                Glide.with(ViewPostActivity@this)
                    .load(uriProfileImage)
                    .into(circularImageView)
            }

            textViewPostProfileName.text = selectedUser.name

            // show post data
            val uriPostImage: Uri = Uri.parse(selectPost.photo.toString())
            Glide.with(ViewPostActivity@this)
                .load(uriPostImage)
                .into(imageView)
            textViewPostProfileDescription.text = selectPost.description

            // config feed
            val feed = Feed()
            feed.id = selectPost.id
            feed.description = selectPost.description
            feed.photo = selectPost.photo
            feed.username = loggedUser.name
            feed.userPhoto = loggedUser.photo

            // config like button
            val likesRef = FirebaseConfig.database
                .child("liked-posts")
                .child("${selectPost.id}")
            likesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var likes = 0
                    if (snapshot.hasChild("likes")) {
                        val likedFeed = snapshot.getValue(LikedFeed::class.java)
                        likes = likedFeed?.likes!!
                    }

                    val liked = LikedFeed()
                    liked.feed = feed
                    liked.user = loggedUser
                    liked.likes = likes

                    // verify if the post was already liked
                    likeButton.isLiked = snapshot.hasChild(loggedUser.id.toString())

                    // add event to like a post
                    likeButton.setOnLikeListener(object: OnLikeListener {
                        override fun liked(likeButton: LikeButton?) {
                            liked.save()
                            textViewPostProfileLikes.text = "${liked.likes} Likes"
                        }

                        override fun unLiked(likeButton: LikeButton?) {
                            liked.removeLike()
                            textViewPostProfileLikes.text = "${liked.likes} Likes"
                        }

                    })

                    textViewPostProfileLikes.text = "${liked.likes} Likes"

                }

                override fun onCancelled(error: DatabaseError) {}

            })

            // add comments click event
            commentImageView.setOnClickListener {
                val intent = Intent(applicationContext, CommentActivity::class.java)
                intent.putExtra(CommentActivity.ID, feed.id)
                startActivity(intent)
            }

        }

    }

    private fun initComponents() {
        circularImageView = findViewById(R.id.imageViewPostFeedImage)
        imageView = findViewById(R.id.imageViewPostFeedMainImage)
        textViewPostProfileName = findViewById(R.id.textViewPostFeedName)
        textViewPostProfileLikes = findViewById(R.id.textViewPostFeedLikes)
        textViewPostProfileDescription = findViewById(R.id.textViewPostFeedDescription)
        likeButton = findViewById(R.id.feed_like_button)
        commentImageView = findViewById(R.id.imageViewFeedCommentImage)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

}