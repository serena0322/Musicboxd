package com.example.musicboxd.reviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReviewFeedItem(
    val reviewId: String = "",
    val userId: String = "",
    val username: String = "",
    val songTitle: String = "",
    val artistName: String = "",
    val rating: Double? = null,
    val reviewText: String? = null,
    val coverUrl: String? = null,
    val timestamp: Date? = null
)

class ReviewsFeedAdapter(
    private val items: MutableList<ReviewFeedItem>,
    private val onClick: (ReviewFeedItem) -> Unit,
    private val onLongClick: (ReviewFeedItem) -> Unit
) : RecyclerView.Adapter<ReviewsFeedAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.cover)
        val title: TextView = view.findViewById(R.id.title)
        val subtitle: TextView = view.findViewById(R.id.subtitle)
        val username: TextView = view.findViewById(R.id.username)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val reviewText: TextView = view.findViewById(R.id.reviewText)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review_feed, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.title.text = item.songTitle
        holder.subtitle.text = item.artistName
        holder.username.text = "@${item.username}"
        holder.reviewText.text = item.reviewText ?: ""

        holder.ratingBar.rating = (item.rating ?: 0.0).toFloat()
        holder.ratingBar.setIsIndicator(true)

        val df = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        holder.time.text = item.timestamp?.let { df.format(it) } ?: ""

        Glide.with(holder.itemView)
            .load(item.coverUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(holder.cover)

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener { onLongClick(item); true }
    }

    override fun getItemCount(): Int = items.size
}
