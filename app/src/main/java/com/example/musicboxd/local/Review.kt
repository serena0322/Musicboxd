package com.example.musicboxd.local

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Review(
    var documentId: String = "",

    @get:PropertyName("action")
    @set:PropertyName("action")
    var actionType: String = "",

    @get:PropertyName("artist")
    @set:PropertyName("artist")
    var artistName: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var songTitle: String = "",

    var sourceUserId: String = "",

    @get:PropertyName("cover")
    @set:PropertyName("cover")
    var albumCoverUrl: String = "",

    var rating: Double = 0.0,

    @get:PropertyName("textReview")
    @set:PropertyName("textReview")
    var reviewText: String = "",

    var timestamp: Timestamp? = null
)
{
    constructor() : this("", "", "", "", "", "", 0.0, "", null)
}

