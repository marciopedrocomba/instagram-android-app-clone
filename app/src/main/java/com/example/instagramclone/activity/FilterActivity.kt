package com.example.instagramclone.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapter.FilterAdapter
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.helper.RecyclerItemClickListener
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import java.io.ByteArrayOutputStream

class FilterActivity : AppCompatActivity() {

    companion object {
        const val CHOSEN_IMAGE = "chosen_image"
        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    private lateinit var imageViewChosenImage: ImageView
    private var image: Bitmap? = null
    private var filterImage: Bitmap? = null

    private lateinit var filtersList: ArrayList<ThumbnailItem>
    private lateinit var loggedUserId: String
    private var loggedUser: User? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var filterAdapter: FilterAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewFilterDescription: TextInputEditText


    private lateinit var usersRef: DatabaseReference
    private lateinit var loggedUserRef: DatabaseReference
    private val firebaseRef: DatabaseReference = FirebaseConfig.database
    private lateinit var followersSnapShot: DataSnapshot

    private var loading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        // basic configurations
        filtersList = ArrayList()
        loggedUserId = FirebaseUser.getUserId()
        usersRef = firebaseRef.child("users")
        getPostData()

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Filter"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        // init components
        imageViewChosenImage = findViewById(R.id.imageViewFilterChosenImage)
        textViewFilterDescription = findViewById(R.id.textViewFilterDescription)
        recyclerView = findViewById(R.id.recyclerViewFilters)
        progressBar = findViewById(R.id.progressBarPost)
        progressBar.visibility = View.GONE

        // get user selected image
        val bundle: Bundle? = intent?.extras

        if (bundle != null) {

            val stream: ByteArray? = bundle.getByteArray(CHOSEN_IMAGE)
            image = BitmapFactory.decodeByteArray(stream, 0, stream?.size!!)
            imageViewChosenImage.setImageBitmap(image)

            filterImage = image?.copy(image?.config, true)

            // config filters recyclerView
            filterAdapter = FilterAdapter(applicationContext, filtersList)
            val layoutManager = LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = filterAdapter

            // add click event to recyclerView
            recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    applicationContext,
                    recyclerView,
                    object: RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            val item = filtersList[position]

                            filterImage = image?.copy(image?.config, true)
                            val filter: Filter = item.filter
                            imageViewChosenImage.setImageBitmap(filter.processFilter(filterImage))
                        }

                        override fun onItemLongClick(view: View?, position: Int) {
                            val item = filtersList[position]
                            Toast.makeText(
                                applicationContext,
                                item.filterName,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                )
            )

            // get filters
            getFilters()


        }

    }

    private fun getFilters() {
        // clear filters list
        ThumbnailsManager.clearThumbs()
        filtersList.clear()

        // config normal filter
        val item = ThumbnailItem()
        item.image = image
        item.filterName = "Normal"
        ThumbnailsManager.addThumb(item)

        // list all filters
        val filters: List<Filter> = FilterPack.getFilterPack(applicationContext)
        for (filter in filters) {
            val filterItem = ThumbnailItem()
            filterItem.image = image
            filterItem.filter = filter
            filterItem.filterName = filter.name
            ThumbnailsManager.addThumb(filterItem)
        }

        filtersList.addAll(ThumbnailsManager.processThumbs(applicationContext))
        filterAdapter.notifyDataSetChanged()

    }

    private fun savePost() {

        if (loading) {

            Snackbar.make(
                recyclerView,
                "Wait till loading is done!",
                Snackbar.LENGTH_SHORT
            ).show()

        }else {

            loading = true

            val post = Post()
            post.userId = loggedUserId
            post.description = textViewFilterDescription.text.toString()

            // create the image Byte Array
            val stream = ByteArrayOutputStream()
            filterImage?.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageBytes: ByteArray = stream.toByteArray()

            // save image on firebase storage
            val storageRef: StorageReference = FirebaseConfig.storage
            val imageRef = storageRef
                .child("images")
                .child("posts")
                .child("${post.id}.jpeg")

            // upload image
            val uploadTask = imageRef.putBytes(imageBytes)

            progressBar.visibility = View.VISIBLE

            uploadTask.addOnFailureListener {

                Toast.makeText(
                    FilterActivity@this,
                    "Unable to save post",
                    Toast.LENGTH_SHORT
                ).show()

            }.addOnSuccessListener {

                imageRef.downloadUrl.addOnCompleteListener { task ->
                    val url: Uri? = task.result
                    url?.let {
                        post.photo = url.toString()
                    }

                    // update posts quantity
                    val postQuantity = loggedUser?.posts!! + 1
                    loggedUser?.posts = postQuantity
                    loggedUser?.updatePostQuantity()

                    if (post.save(followersSnapShot)) {
                        progressBar.visibility = View.GONE
                        loading = false
                        finish()
                    }

                }

            }

        }

    }

    private fun getPostData() {
        loggedUserRef = usersRef.child(loggedUserId)
        loggedUserRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // get logged user data
                loggedUser = snapshot.getValue(User::class.java)

                /*get logged user followers*/
                val followersRef = firebaseRef
                    .child("followers")
                    .child(loggedUserId)
                followersRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        followersSnapShot = snapshot
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter_save -> {
                savePost()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}