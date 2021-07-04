package com.example.instagramclone.model

data class Feed(
    var id: String?,
    var photo: String?,
    var description: String?,
    var username: String?,
    var userPhoto: String?
) {

    constructor(): this(null, null, null, null, null)

}