package com.example.instagramclone.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.adapter.GridAdapter
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FriendProfileActivity : AppCompatActivity() {

    companion object {
        const val SELECTED_USER = "selected_user"
    }

    private lateinit var textPosts: TextView
    private lateinit var textFollowers: TextView
    private lateinit var textFollowing: TextView
    private lateinit var actionProfileButton: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var gridViewProfile: GridView
    private lateinit var gridAdapter: GridAdapter

    private var selectedUser: User? = null
    private var loggedUser: User? = null
    private lateinit var usersRef: DatabaseReference
    private lateinit var friendUserRef: DatabaseReference
    private lateinit var followersRef: DatabaseReference
    private lateinit var loggedUserRef: DatabaseReference
    private lateinit var userPostsRef: DatabaseReference
    private val firebaseRef: DatabaseReference = FirebaseConfig.database
    private val loggedUserId: String = FirebaseUser.getLoggedUserData().id.toString()

    private lateinit var postsList: ArrayList<Post>

    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        // initial configurations
        usersRef = firebaseRef.child("users")
        followersRef = firebaseRef.child("followers")

        // init components
        initComponents()

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Profile"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        // get selected user
        val selectedUserDataJson = intent?.extras?.getString(SELECTED_USER)
        if (selectedUserDataJson != null) {
            selectedUser = Json.decodeFromString(selectedUserDataJson) as User

            // selected user posts ref
            userPostsRef = FirebaseConfig.database
                .child("posts")
                .child(selectedUser?.id.toString())

            supportActionBar?.title = "${selectedUser?.name}"

            // set user photo
            val image = selectedUser?.photo
            if (image == null) {
                profileImage.setImageResource(R.drawable.avatar)
            }else {
                val url: Uri = Uri.parse(image)
                Glide.with(applicationContext)
                    .load(url)
                    .into(profileImage)
            }
        }

        //init image loader
        initImageLoader()

        loadPostsImages()

        // open clicked post
        gridViewProfile.setOnItemClickListener { parent, view, position, id ->
            val post = postsList[position]
            val intent = Intent(applicationContext, ViewPostActivity::class.java)

            val postJsonData = Json.encodeToString(post)
            val selectedUserJsonData = Json.encodeToString(selectedUser)

            intent.putExtra(ViewPostActivity.SELECTED_POST, postJsonData)
            intent.putExtra(ViewPostActivity.SELECTED_USER, selectedUserJsonData)

            startActivity(intent)

        }

    }

    private fun initImageLoader() {
        val config: ImageLoaderConfiguration = ImageLoaderConfiguration
            .Builder(this)
            .memoryCache(LruMemoryCache(2 * 1024 * 1024))
            .memoryCacheSize(2 * 1024 * 1024)
            .memoryCacheSizePercentage(13) // default
            .diskCache(UnlimitedDiskCache(cacheDir)) // default
            .diskCacheSize(50 * 1024 * 1024)
            .diskCacheFileCount(100)
            .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
            .build()
        ImageLoader.getInstance().init(config)
    }

    private fun loadPostsImages() {

        postsList = ArrayList()

        userPostsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gridSize = resources.displayMetrics.widthPixels
                val imageSize = gridSize / 3
                gridViewProfile.columnWidth = imageSize

                val imagesUrl = ArrayList<String>()

                for (data in snapshot.children) {
                    val post = data.getValue(Post::class.java)
                    if (post != null) {
                        postsList.add(post)
                        imagesUrl.add(post.photo!!)
                    }
                }

                // config adapter for gridView
                gridAdapter = GridAdapter(applicationContext, R.layout.grid_post, imagesUrl)
                gridViewProfile.adapter = gridAdapter

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getLoggedUserData() {
        loggedUserRef = usersRef.child(loggedUserId)
        loggedUserRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // get logged user data
                loggedUser = snapshot.getValue(User::class.java)

                if (loggedUser != null) verifyFollowingFriendUser()

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun verifyFollowingFriendUser() {

        val followerRef = followersRef
            .child(selectedUser?.id.toString())
            .child(loggedUserId)
        followerRef.addListenerForSingleValueEvent(object: ValueEventListener  {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // already following the user
                    enableFollowButton(true)
                }else {
                    // not following the user
                    enableFollowButton(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun enableFollowButton(state: Boolean) {
        if (state) {
            actionProfileButton.text = "Following"
        }else {
            actionProfileButton.text = "Follow"

            // add event to follow user
            actionProfileButton.setOnClickListener {
                // save follower
                saveFollower(loggedUser!!, selectedUser!!)
            }

        }
    }

    private fun saveFollower(loggedU: User, selectedU: User) {

        val loggedUserNecessaryData = HashMap<String, Any>()
        loggedUserNecessaryData["name"] = loggedU.name.toString()
        loggedUserNecessaryData["photo"] = loggedU.photo.toString()

        val followerRef = followersRef
            .child(selectedU.id.toString())
            .child(loggedU.id.toString())
        followerRef.setValue(loggedUserNecessaryData)

        // update the follow/following button
        actionProfileButton.text = "Following"
        actionProfileButton.setOnClickListener(null)

        // +1 following to logged user
        val following: Int = loggedU.following + 1
        val followingData = HashMap<String, Any>()
        followingData["following"] = following

        val followingUserRef = usersRef
            .child(loggedU.id.toString())
        followingUserRef.updateChildren(followingData)

        // +1 followers to selected user
        val followers: Int = selectedU.followers + 1
        val followersData = HashMap<String, Any>()
        followersData["followers"] = followers

        val followerUserRef = usersRef
            .child(selectedU.id.toString())
        followerUserRef.updateChildren(followersData)
    }

    override fun onStart() {
        super.onStart()
        getFriendUserDataProfile()

        // get logged user data
        getLoggedUserData()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListener != null) friendUserRef.removeEventListener(valueEventListener!!)
    }

    private fun getFriendUserDataProfile() {
        friendUserRef = usersRef.child(selectedUser?.id.toString())
        valueEventListener = friendUserRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {

                    val posts = user.posts
                    val following = user.following
                    val followers = user.followers

                    textPosts.text = "$posts"
                    textFollowing.text = "$following"
                    textFollowers.text = "$followers"

                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun initComponents() {
        textPosts = findViewById(R.id.textViewPosts)
        textFollowers = findViewById(R.id.textViewFollowers)
        textFollowing = findViewById(R.id.textViewFollowing)
        profileImage = findViewById(R.id.imageViewProfile)
        actionProfileButton = findViewById(R.id.buttonProfileAction)
        actionProfileButton.text = "Loading"
        gridViewProfile = findViewById(R.id.gridViewProfile)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

}