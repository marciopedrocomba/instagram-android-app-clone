package com.example.instagramclone.model

import com.example.instagramclone.helper.FirebaseConfig
import kotlin.math.abs

data class LikedFeed(
    var feed: Feed?,
    var user: User?,
    var likes: Int = 0
) {
    constructor(): this(null, null,0)

    fun save() {
        val firebaseRef = FirebaseConfig.database

        // user object
        val userData = HashMap<String, Any>()
        userData["username"] = this.user?.name!!
        userData["userPhoto"] = this.user?.photo!!

        val likedPostsRef = firebaseRef
            .child("liked-posts")
            .child("${this.feed?.id}")
            .child("${this.user?.id}")
        likedPostsRef.setValue(userData)

        // update likes quantities
        updateLikesQuantity(1)

    }

    fun removeLike() {
        val firebaseRef = FirebaseConfig.database

        val likedPostsRef = firebaseRef
            .child("liked-posts")
            .child("${this.feed?.id}")
            .child("${this.user?.id}")
        likedPostsRef.removeValue()

        // update likes quantities
        updateLikesQuantity(-1)
    }

    private fun updateLikesQuantity(value: Int) {
        val firebaseRef = FirebaseConfig.database
        val likedPostsRef = firebaseRef
            .child("liked-posts")
            .child("${this.feed?.id}")
            .child("likes")
        this.likes += value
        likedPostsRef.setValue(this.likes)
    }

}