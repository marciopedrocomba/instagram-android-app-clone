package com.example.instagramclone.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.activity.EditProfileActivity
import com.example.instagramclone.activity.ViewPostActivity
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ProfileFragment : Fragment() {

    private lateinit var textPosts: TextView
    private lateinit var textFollowers: TextView
    private lateinit var textFollowing: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var editProfileButton: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var gridViewProfile: GridView

    private var loggedUser: User? = null

    private val firebaseRef: DatabaseReference = FirebaseConfig.database
    private lateinit var usersRef: DatabaseReference
    private lateinit var loggedUserRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null
    private lateinit var userPostsRef: DatabaseReference

    private lateinit var gridAdapter: GridAdapter
    private val postsList = ArrayList<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        //initial configurations
        loggedUser = FirebaseUser.getLoggedUserData()
        usersRef = firebaseRef.child("users")

        // selected user posts ref
        userPostsRef = FirebaseConfig.database
            .child("posts")
            .child(loggedUser?.id.toString())

        // components configuration
        initComponents(view)

        editProfileButton.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // open clicked post
        gridViewProfile.setOnItemClickListener { parent, view, position, id ->
            val post = postsList[position]
            val intent = Intent(requireContext(), ViewPostActivity::class.java)

            val postJsonData = Json.encodeToString(post)
            val selectedUserJsonData = Json.encodeToString(loggedUser)

            intent.putExtra(ViewPostActivity.SELECTED_POST, postJsonData)
            intent.putExtra(ViewPostActivity.SELECTED_USER, selectedUserJsonData)

            startActivity(intent)

        }

        // init image loader
        initImageLoader()

        // load user posts
        loadPostsImages()

        return view
    }

    private fun getLoggedUserPhoto() {
        // set user photo
        val image = loggedUser?.photo
        if (image == null) {
            profileImage.setImageResource(R.drawable.avatar)
        }else {
            val url: Uri = Uri.parse(image)
            Glide.with(requireActivity())
                .load(url)
                .into(profileImage)
        }
    }

    private fun initImageLoader() {
        val config: ImageLoaderConfiguration = ImageLoaderConfiguration
            .Builder(activity)
            .memoryCache(LruMemoryCache(2 * 1024 * 1024))
            .memoryCacheSize(2 * 1024 * 1024)
            .memoryCacheSizePercentage(13) // default
            .diskCache(UnlimitedDiskCache(activity?.cacheDir)) // default
            .diskCacheSize(50 * 1024 * 1024)
            .diskCacheFileCount(100)
            .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
            .build()
        ImageLoader.getInstance().init(config)
    }

    private fun loadPostsImages() {
        userPostsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gridSize = resources.displayMetrics.widthPixels
                val imageSize = gridSize / 3
                gridViewProfile.columnWidth = imageSize

                postsList.clear()
                val imagesUrl = ArrayList<String>()

                for (data in snapshot.children) {
                    val post = data.getValue(Post::class.java)
                    if (post != null) {
                        postsList.add(post)
                        imagesUrl.add(post.photo!!)
                    }
                }

                // config adapter for gridView
                gridAdapter = GridAdapter(requireActivity(), R.layout.grid_post, imagesUrl)
                gridViewProfile.adapter = gridAdapter

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getLoggedUserDataProfile() {
        loggedUserRef = usersRef.child(loggedUser?.id.toString())
        valueEventListener = loggedUserRef.addValueEventListener(object: ValueEventListener {
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

    override fun onStart() {
        super.onStart()
        getLoggedUserPhoto()
        getLoggedUserDataProfile()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListener != null) loggedUserRef.removeEventListener(valueEventListener!!)
    }

    private fun initComponents(view: View) {
        textPosts = view.findViewById(R.id.textViewPosts)
        textFollowers = view.findViewById(R.id.textViewFollowers)
        textFollowing = view.findViewById(R.id.textViewFollowing)
        progressBar = view.findViewById(R.id.progressBarProfile)
        editProfileButton = view.findViewById(R.id.buttonProfileAction)
        profileImage = view.findViewById(R.id.imageViewProfile)
        gridViewProfile = view.findViewById(R.id.gridViewProfile)
    }

}