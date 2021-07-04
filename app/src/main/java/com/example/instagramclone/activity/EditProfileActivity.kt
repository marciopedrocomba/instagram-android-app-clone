package com.example.instagramclone.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.decodeBitmap
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.helper.Permission
import com.example.instagramclone.model.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )


    private lateinit var imageViewEditProfile: CircleImageView
    private lateinit var textViewEditProfilePhoto: TextView
    private lateinit var editTextProfileName: EditText
    private lateinit var editTextProfileEmail: EditText
    private lateinit var buttonSaveChanges: Button
    private lateinit var progressBarProfileUploadImage: ProgressBar

    private lateinit var loggedUser: User
    private val storageRef = FirebaseConfig.storage
    private lateinit var userId: String

    private val photoPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK) {

            var image: Bitmap? = null

            try {

                val selectedImageUri = result.data?.data

                selectedImageUri?.let {

                    image = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(contentResolver , selectedImageUri)
                    }else {
                        val source = ImageDecoder.createSource(contentResolver, selectedImageUri)
                        ImageDecoder.decodeBitmap(source)
                    }

                }

            }catch (e: Exception) {
                e.printStackTrace()
            }

            if (image != null) {
                imageViewEditProfile.setImageBitmap(image)
                uploadImageFirebase(image!!)
            }

        }

    }

    private val cameraCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == RESULT_OK) {

            var image: Bitmap? = null

            try {
                image = result.data?.extras?.get("data") as Bitmap
            }catch (e: Exception) {
                e.printStackTrace()
            }

            if (image != null) {
                imageViewEditProfile.setImageBitmap(image)
                uploadImageFirebase(image!!)
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        loggedUser = FirebaseUser.getLoggedUserData()
        userId = FirebaseUser.getUserId()

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Edit Profile"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        initComponents()

        Permission.validatePermission(this, permissions, 1)

        progressBarProfileUploadImage.visibility = View.GONE

        val userProfile = FirebaseUser.getActualUser()
        editTextProfileName.setText(userProfile.displayName)
        editTextProfileEmail.setText(userProfile.email)

        val imageUrl: Uri? = userProfile.photoUrl

        if (imageUrl == null) {
            imageViewEditProfile.setImageResource(R.drawable.avatar)
        } else {
            Glide
                .with(applicationContext)
                .load(imageUrl)
                .into(imageViewEditProfile)
        }

        // save user updates
        buttonSaveChanges.setOnClickListener {
            val name = editTextProfileName.text.toString()
            val email = editTextProfileEmail.text.toString()

            // update profile name
            FirebaseUser.updateUserName(name)

            // update on the database
            loggedUser.name = name
            loggedUser.update()

            Toast.makeText(
                applicationContext,
                "Data updated successfully",
                Toast.LENGTH_SHORT
            ).show()


        }

        // pick an image from gallery
        imageViewEditProfile.setOnClickListener {
            launcherChooser()
        }

        textViewEditProfilePhoto.setOnClickListener {
            launcherChooser()
        }

    }

    private fun initComponents() {
        imageViewEditProfile = findViewById(R.id.imageViewEditProfile)
        textViewEditProfilePhoto = findViewById(R.id.textViewEditProfilePhoto)
        editTextProfileName = findViewById(R.id.editTextEditProfileName)
        editTextProfileEmail = findViewById(R.id.editTextEditProfileEmail)
        buttonSaveChanges = findViewById(R.id.buttonSaveEditProfileChanges)
        progressBarProfileUploadImage = findViewById(R.id.progressBarProfileUploadImage)
    }

    private fun launcherChooser() {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)

        val linearLayoutOpenCamera: LinearLayout? =
            bottomSheetDialog.findViewById(R.id.linearLayoutOpenCamera)

        val linearLayoutOpenGallery: LinearLayout? =
            bottomSheetDialog.findViewById(R.id.linearLayoutOpenGallery)

        linearLayoutOpenGallery?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(packageManager) != null) {
                photoPickLauncher.launch(intent)
                bottomSheetDialog.dismiss()
            }
        }

        linearLayoutOpenCamera?.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                cameraCaptureLauncher.launch(intent)
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()

        /*val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder.setTitle("Choose")
        builder.setMessage("Do want to open the camera or gallery?")

        builder.setPositiveButton("Gallery"
        ) { _, _ ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(packageManager) != null) {
                photoPickLauncher.launch(intent)
            }
        }

        builder.setNegativeButton("Camera"
        ) { _, _ ->
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                cameraCaptureLauncher.launch(intent)
            }
        }

        builder.create()
        builder.show()*/
    }

    private fun uploadImageFirebase(image: Bitmap) {

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageBytes: ByteArray = stream.toByteArray()

        // image in firebase storage ref
        val imageRef = storageRef
            .child("images")
            .child("profile")
            .child("${userId}.jpeg")

        // upload image
        val uploadTask = imageRef.putBytes(imageBytes)

        progressBarProfileUploadImage.visibility = View.VISIBLE

        uploadTask.addOnFailureListener {

            Toast.makeText(
                applicationContext,
                "Error uploading image",
                Toast.LENGTH_SHORT
            ).show()

        }/*.addOnProgressListener {

            val totalBytes = it.totalByteCount
            val transferredBytes = it.bytesTransferred
            val percentage = ((transferredBytes / totalBytes) * 100).toInt()
            progressBarProfileUploadImage.progress = percentage
            println("TEST: $percentage")

        }*/.addOnSuccessListener {

            Snackbar.make(
                buttonSaveChanges,
                "Success uploading image",
                Snackbar.LENGTH_SHORT
            ).show()

            progressBarProfileUploadImage.visibility = View.GONE

            imageRef.downloadUrl.addOnCompleteListener { task ->
                val url: Uri? = task.result
                if (url != null) {
                    updateUserPhoto(url)
                }
            }

        }

    }

    private fun updateUserPhoto(url: Uri) {
        // update profile photo
        FirebaseUser.updateUserPhoto(url)

        // update photo on firebase
        loggedUser.photo = url.toString()
        loggedUser.update()

        Snackbar.make(
            buttonSaveChanges,
            "Profile image updated!",
            Snackbar.LENGTH_SHORT
        ).show()

    }

    private fun alertPermissionValidation() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder.setTitle("Denied permissions")
        builder.setMessage("To use this app you need to accept all permissions")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirm"
        ) { _, _ ->
            finish()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (resultPermission in grantResults) {
            if (resultPermission == PackageManager.PERMISSION_DENIED) {
                alertPermissionValidation()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }


}