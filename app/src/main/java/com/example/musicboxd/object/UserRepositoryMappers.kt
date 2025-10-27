package com.example.musicboxd.`object`

import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.PlaylistItem
import com.example.musicboxd.local.Review
import com.google.firebase.Timestamp


//per instrumented test
internal fun mapReview(docId: String, data: Map<String, Any?>, userId: String): Review? {
    val songTitle = data["title"] as? String ?: return null
    val artistName = data["artist"] as? String ?: return null
    val timestamp = data["timestamp"] as? Timestamp
    val rating = (data["rating"] as? Number)?.toDouble() ?: 0.0
    val reviewText = data["textReview"] as? String ?: ""
    val cover = data["cover"] as? String ?: ""
    return Review(
        documentId = docId,
        actionType = "review",
        artistName = artistName,
        songTitle = songTitle,
        sourceUserId = userId,
        albumCoverUrl = cover,
        rating = rating,
        reviewText = reviewText,
        timestamp = timestamp
    )
}

internal fun mapPlaylistItem(docId: String, data: Map<String, Any?>): PlaylistItem? {
    val name = data["name"] as? String ?: return null
    val createdBy = data["createdBy"] as? String ?: ""
    val timestamp = data["timestamp"] as? Timestamp
    val tracks = data["tracks"] as? List<String> ?: emptyList()
    return PlaylistItem(
        id = docId,
        name = name,
        createdBy = createdBy,
        timestamp = timestamp,
        tracks = tracks
    )
}

internal fun mapActivity(action: String?, timestamp: Timestamp?): ActivityItem? {
    if (action == null || timestamp == null) return null
    return ActivityItem(content = action, timestamp = timestamp)
}
