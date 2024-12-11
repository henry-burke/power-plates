package com.cs407.powerplates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.History

class HistoryAdapter(
    private val historyList: List<History>,
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.history_card_title)
        val ex1: TextView = itemView.findViewById(R.id.exercise_name_1)
        val ex2: TextView = itemView.findViewById(R.id.exercise_name_2)
        val ex3: TextView = itemView.findViewById(R.id.exercise_name_3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currExercise: History = historyList[position]

        "${currExercise.category} Workout on ${currExercise.date}".also { holder.title.text = it }
        holder.ex1.text = currExercise.exercise1
        holder.ex2.text = currExercise.exercise2
        holder.ex3.text = currExercise.exercise3
    }
}
