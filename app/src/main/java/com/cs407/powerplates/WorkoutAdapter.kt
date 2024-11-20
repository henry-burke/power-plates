package com.cs407.powerplates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val onClick: (String) -> Unit,  // Updated to expect a String (workoutName)
    private val workList: List<WorkoutType>
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    // ViewHolder class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exItems: TextView = itemView.findViewById(R.id.excersize)
        val difItems: TextView = itemView.findViewById(R.id.difficulty)
        val musItems: TextView = itemView.findViewById(R.id.muscleGrp)
    }

    // Create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_layout, parent, false)
        return ViewHolder(itemView)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wType: WorkoutType = workList[position]

        // Bind workout details to the views
        holder.exItems.text = wType.excersizeName
        holder.difItems.text = wType.difficulty
        holder.musItems.text = wType.muscleGrp

        // Set onClickListener to pass workoutName to the callback
        holder.itemView.setOnClickListener { onClick(wType.excersizeName) }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return workList.size
    }
}
