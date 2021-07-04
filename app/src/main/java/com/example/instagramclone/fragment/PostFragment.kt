package com.example.instagramclone.fragment

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.R
import com.example.instagramclone.activity.FilterActivity
import com.example.instagramclone.helper.Permission
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class PostFragment : Fragment() {

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private lateinit var buttonOpenGallery: Button
    private lateinit var buttonOpenCamera: Button

    private val photoPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == AppCompatActivity.RESULT_OK) {

            var image: Bitmap? = null

            try {

                val selectedImageUri = result.data?.data

                selectedImageUri?.let {

                    image = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImageUri)
                    }else {
                        val source = ImageDecoder.createSource(activity?.contentResolver!!, selectedImageUri)
                        ImageDecoder.decodeBitmap(source)
                    }

                }

            }catch (e: Exception) {
                e.printStackTrace()
            }

            if (image != null) {
                openFilterActivity(image!!)
            }

        }

    }

    private val cameraCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == AppCompatActivity.RESULT_OK) {

            var image: Bitmap? = null

            try {
                image = result.data?.extras?.get("data") as Bitmap
            }catch (e: Exception) {
                e.printStackTrace()
            }

            if (image != null) {
                openFilterActivity(image)
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_post, container, false)

        Permission.validatePermission(requireActivity(), permissions, 1)

        buttonOpenGallery = view.findViewById(R.id.buttonPostOpenGallery)
        buttonOpenCamera = view.findViewById(R.id.buttonPostOpenCamera)

        // add click event to open camera
        buttonOpenCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                cameraCaptureLauncher.launch(intent)
            }
        }

        // add click event to open gallery
        buttonOpenGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                photoPickLauncher.launch(intent)
            }
        }

        return view
    }

    private fun openFilterActivity(image: Bitmap) {

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageBytes: ByteArray = stream.toByteArray()

        // send image data to apply filter
        val intent = Intent(activity, FilterActivity::class.java)
        intent.putExtra(FilterActivity.CHOSEN_IMAGE, imageBytes)
        startActivity(intent)

    }

}