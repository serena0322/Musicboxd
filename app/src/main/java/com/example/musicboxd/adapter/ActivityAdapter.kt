package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.classes.ActivityItem
import com.example.musicboxd.R

class ActivityAdapter(private var items: List<ActivityItem>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.activityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.content.text = items[position].content
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ActivityItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
