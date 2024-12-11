package com.cs407.powerplates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LapAdapter(private val laps: List<String>) : RecyclerView.Adapter<LapAdapter.LapViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lap_item, parent, false)
        return LapViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapViewHolder, position: Int) {
        holder.lapText.text = laps[position]
    }

    override fun getItemCount(): Int = laps.size

    class LapViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lapText: TextView = view.findViewById(R.id.lapText)
    }
}
