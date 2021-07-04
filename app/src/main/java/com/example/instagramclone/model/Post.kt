package com.example.instagramclone.model

import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.google.firebase.database.DataSnapshot
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    var id: String?,
    var userId: String?,
    var description: String?,
    var photo: String?
) {

    constructor(): this(null, null, null, null) {
        val firebaseRef = FirebaseConfig.database
        val postRef = firebaseRef.child("posts")
        val postId = postRef.push().key
        this.id = postId
    }

    fun save(snapshot: DataSnapshot): Boolean {

        val obj = HashMap<String, Any>()
        val loggedUser = FirebaseUser.getLoggedUserData()

        val firebaseRef = FirebaseConfig.database

        // post reference
        obj["/posts/${this.userId}/${this.id}"] = this

        // post reference
        for (follower in snapshot.children) {

            val followerId = follower.key

            val followerData = HashMap<String, Any>()
            followerData["photo"] = this.photo.toString()
            followerData["description"] = this.description.toString()
            followerData["id"] = this.id.toString()
            followerData["username"] = loggedUser.name.toString()
            followerData["userPhoto"] = loggedUser.photo.toString()

            obj["/feed/${followerId}/${this.id}"] = followerData

        }

        firebaseRef.updateChildren(obj)
        return true
    }

}