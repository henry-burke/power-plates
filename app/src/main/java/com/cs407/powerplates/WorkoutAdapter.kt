package com.cs407.powerplates

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.Exercise
import com.cs407.powerplates.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutAdapter(
    private val onClick: (List<String>) -> Unit,  // Updated to expect a String (workoutName)
    private val workList: List<WorkoutType>,
    private val savedWorkouts: ArrayList<String>
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

        // highlight if already selected, default color (white for now) if otherwise
        if (savedWorkouts.contains(wType.exerciseName)) {
            holder.itemView.setBackgroundColor(Color.argb(255, 50, 255, 50))
        } else {
            holder.itemView.setBackgroundColor(Color.argb(255, 255, 255, 255))
        }

        // Set onClickListener to pass workoutName to the callback
        holder.itemView.setOnClickListener {
            if(savedWorkouts.contains(wType.exerciseName)) {
                // deselect workout
                savedWorkouts.remove(wType.exerciseName)
                onClick(listOf(wType.exerciseName, wType.difficulty, wType.muscleGrp))
            } else if(savedWorkouts.size < 3) {
                // select workout
                savedWorkouts.add(wType.exerciseName)
                onClick(listOf(wType.exerciseName, wType.difficulty, wType.muscleGrp))
            }
            notifyItemChanged(position)
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return workList.size
    }
}