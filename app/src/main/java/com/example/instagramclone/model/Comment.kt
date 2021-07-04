package com.example.instagramclone.model

import com.example.instagramclone.helper.FirebaseConfig

data class Comment(
    var id: String?,
    var postId: String?,
    var userId: String?,
    var userPhoto: String?,
    var username: String?,
    var comment: String?
) {
    constructor(): this(null, null, null, null, null, null)

    fun save(): Boolean {
        val commentRef = FirebaseConfig.database
            .child("comments")
            .child("${this.postId}")
        val commentKey = commentRef.push().key
        this.id = commentKey
        commentRef.child("${this.id}").setValue(this)
        return true
    }

}