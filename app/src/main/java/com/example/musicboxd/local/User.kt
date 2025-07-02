package com.example.musicboxd.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity(tableName = "User")
data class User(
    @PrimaryKey val id: String = "",
    var username: String = "",
    val email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    val followers: Long = 0,
    val following: Long = 0,
    val createdAt: Timestamp = Timestamp.now(),
) {
    constructor() : this("", "", "", "", "", 0, 0, Timestamp.now())
}


