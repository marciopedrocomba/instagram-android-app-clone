package com.example.instagramclone.model

import com.example.instagramclone.helper.FirebaseConfig
import com.google.firebase.database.Exclude
import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: String?,
    var name: String?,
    var email: String?,
    var password: String?,
    var photo: String?,
    var followers: Int = 0,
    var following: Int = 0,
    var posts: Int = 0
) {

    constructor(): this(null, null, null, null, null)

    fun save() {
        val firebaseRef = FirebaseConfig.database
        val usersRef = firebaseRef.child("users").child(this.id.toString())

        val user = User()
        user.id = this.id
        user.name = this.name
        user.email = this.email
        user.followers = this.followers
        user.following = this.following
        user.posts = this.posts

        usersRef
            .setValue(user)
    }

    fun updatePostQuantity() {
        val firebaseRef = FirebaseConfig.database
        val usersRef = firebaseRef
            .child("users")
            .child(this.id.toString())

        val data = HashMap<String, Any>()
        data["posts"] = this.posts

        usersRef.updateChildren(data)
    }

    fun update() {
        val firebaseRef = FirebaseConfig.database
        val obj = HashMap<String, Any>()
        obj["/users/${this.id}/name"] = this.name.toString()
        obj["/users/${this.id}/photo"] = this.photo.toString()
        firebaseRef.updateChildren(obj)
    }

    private fun parseToMap(): Map<String, Any> {
        val userMap = HashMap<String, Any>()
        userMap["id"] = this.id.toString()
        userMap["name"] = this.name.toString()
        userMap["email"] = this.email.toString()
        userMap["photo"] = this.photo.toString()
        userMap["followers"] = this.followers
        userMap["following"] = this.following
        userMap["posts"] = this.posts
        return userMap
    }

}