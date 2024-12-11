package com.cs407.powerplates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(
    private val itemsList: List<WorkoutData>
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.itemTitle) // Title
        val ivIcon: ImageView = itemView.findViewById(R.id.itemIcon) // Icon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.tvTitle.text = currentItem.title
        holder.ivIcon.setImageResource(currentItem.iconResId) // Set icon dynamically
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
}
