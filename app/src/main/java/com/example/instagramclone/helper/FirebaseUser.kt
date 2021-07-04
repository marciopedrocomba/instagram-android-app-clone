package com.example.instagramclone.helper

import android.net.Uri
import android.util.Log
import com.example.instagramclone.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import java.lang.Exception

class FirebaseUser {
    companion object {

        fun getActualUser(): FirebaseUser {
            val user = FirebaseConfig.auth
            return user.currentUser!!
        }

        fun updateUserName(name: String) {
            try {

                // logged user
                val user = getActualUser()

                // config the user object to update
                val profile = UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profile)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) Log.d("Profile", "Error updating user profile name")
                    }

            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateUserPhoto(url: Uri) {
            try {

                // logged user
                val user = getActualUser()

                // config the user object to update
                val profile = UserProfileChangeRequest
                    .Builder()
                    .setPhotoUri(url)
                    .build()

                user.updateProfile(profile)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) Log.d("Profile", "Error updating user profile photo")
                    }

            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getLoggedUserData(): User {

            val firebaseUser = getActualUser()

            val user = User()
            user.id = firebaseUser.uid
            user.name = firebaseUser.displayName
            user.email = firebaseUser.email

            if (firebaseUser.photoUrl == null) {
                user.photo = ""
            }else {
                user.photo = firebaseUser.photoUrl.toString()
            }

            return user

        }

        fun getUserId(): String {
            return getActualUser().uid
        }

    }
}