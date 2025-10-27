package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.R
import java.text.SimpleDateFormat
import java.util.Locale

/*Adapter per la RecyclerView che gestisce la visualizzazione delle attività utente
o degli amici all’interno della schermata Activity.*/

class ActivityAdapter(
    private var tabIndex: Int = 0,
    private var items: List<ActivityItem>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    fun updateTabIndex(index: Int) {
        tabIndex = index
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.activityText)
        val date: TextView = itemView.findViewById(R.id.date)  // Nuovo campo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)

        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = items[position]
        holder.content.text = item.content

        val formattedDate = item.timestamp?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Data non disponibile"
        holder.date.text = formattedDate
    }


    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ActivityItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
