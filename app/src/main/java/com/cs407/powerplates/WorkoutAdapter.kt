package com.cs407.powerplates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val onClick: (List<String>) -> Unit,
    private val workList: List<WorkoutType>,
    private var savedWorkouts: ArrayList<String>,
    private val savedWorkoutsCategories: ArrayList<String>
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    // ViewHolder class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.card_lin_layout)
        val exItems: TextView = itemView.findViewById(R.id.exercise_name)
        val difItems: TextView = itemView.findViewById(R.id.level)
        val musItems: TextView = itemView.findViewById(R.id.muscleGrp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wType: WorkoutType = workList[position]

        // Bind workout details to the views
        holder.exItems.text = wType.exerciseName
        holder.difItems.text = wType.difficulty
        holder.musItems.text = wType.muscleGrp

        // Update background drawable based on selection
        if (savedWorkouts.contains(wType.exerciseName)) {
            holder.linearLayout.setBackgroundResource(R.drawable.selected_card_border) // Highlighted border
        } else {
            holder.linearLayout.setBackgroundResource(R.drawable.card_border) // Default border
        }

        // Handle item selection and deselection
        holder.itemView.setOnClickListener {
            val categoryCount = savedWorkoutsCategories.count { it == wType.category }

            if (savedWorkouts.contains(wType.exerciseName)) {
                // Deselect workout
                savedWorkouts.remove(wType.exerciseName)
                savedWorkoutsCategories.remove(wType.category)
            } else if (categoryCount < 3 && savedWorkouts.size < 15) {
                // Select workout
                savedWorkouts.add(wType.exerciseName)
                savedWorkoutsCategories.add(wType.category)
            }

            // Trigger the callback and update the UI
            onClick(listOf(wType.exerciseName, wType.difficulty, wType.muscleGrp))
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return workList.size
    }
}

