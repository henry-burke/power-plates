package com.cs407.powerplates

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.Exercise

class WorkoutAdapter(
    private val onClick: (List<String>) -> Unit,  // Updated to expect a String (workoutName)
    private val workList: List<WorkoutType>,
    private val savedWorkouts: List<WorkoutType>
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    // ViewHolder class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exItems: TextView = itemView.findViewById(R.id.exercise_name)
        val difItems: TextView = itemView.findViewById(R.id.level)
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
        holder.exItems.text = wType.exerciseName
        holder.difItems.text = wType.difficulty
        holder.musItems.text = wType.muscleGrp

        // Set onClickListener to pass workoutName to the callback
        holder.itemView.setOnClickListener { onClick(listOf(wType.exerciseName, wType.difficulty, wType.muscleGrp)) }

        if(savedWorkouts != null) {
            for(workout in savedWorkouts) {
                if(wType.exerciseName == workout.exerciseName) {
                    holder.itemView.setBackgroundColor(Color.argb(255, 50, 255, 50))
                }
            }

        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return workList.size
    }
}