package com.example.musicboxd.classes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "user") class User(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val email: String = "",
    @Ignore val picture: String? = null,
    val bio: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val firstName: String = "",
    val lastName: String = ""
)
