import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Song")
data class Song(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val albumId: String = "",
    val durationSeconds: Int? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null
)

