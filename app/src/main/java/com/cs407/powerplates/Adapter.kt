package com.cs407.powerplates


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class Adapter(
    private val itemsList: List<WorkoutData>
) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvItems : TextView = itemView.findViewById(R.id.tv_item)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvItems.text = itemsList[position].title
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
}
