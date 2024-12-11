package com.cs407.powerplates

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cs407.powerplates.data.Exercise
import com.cs407.powerplates.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutContentFragment : Fragment() {

    private var exerciseName: String? = null // Exercise name passed from another fragment
    private lateinit var exerciseDatabase: ExerciseDatabase // Reference to the database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exerciseName = it.getString(ARG_EXERCISE_NAME)
            Log.v("test", "ALERT: $exerciseName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_workout_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the database
        exerciseDatabase = ExerciseDatabase.getDatabase(requireContext())

        // Fetch the exercise details from the database
        CoroutineScope(Dispatchers.IO).launch {
            val exercise = exerciseName?.let { exerciseDatabase.exerciseDao().getExerciseByName(it) }
            if (exercise != null) {
                updateUI(exercise, view)
            }
        }

    }

    // Function to update the UI with the exercise details
    private suspend fun updateUI(exercise: Exercise, view: View) {
        Log.v("test", "ALERT")
        withContext(Dispatchers.Main) {
            view.findViewById<TextView>(R.id.exercise_name).text = exercise.exerciseName
            view.findViewById<TextView>(R.id.primary_muscle).text = exercise.primaryMuscle
            view.findViewById<TextView>(R.id.secondary_muscle).text = exercise.secondaryMuscle
            view.findViewById<TextView>(R.id.level).text = exercise.level
            view.findViewById<TextView>(R.id.category).text = exercise.category
            view.findViewById<TextView>(R.id.description).text = exercise.description
            view.findViewById<TextView>(R.id.compound).text =
                if (exercise.compound) "Compound" else "Isolated"
            view.findViewById<TextView>(R.id.progression_type).text = exercise.progressionType
            view.findViewById<TextView>(R.id.type).text = exercise.type
        }
    }

    companion object {
        private const val ARG_EXERCISE_NAME = "exercise_name"

        fun newInstance(exerciseName: String): WorkoutContentFragment {
            val fragment = WorkoutContentFragment()
            val bundle = Bundle().apply {
                putString(ARG_EXERCISE_NAME, exerciseName)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}

//package com.cs407.powerplates
//
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import com.cs407.powerplates.data.Exercise
//import com.cs407.powerplates.data.ExerciseDatabase
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.lang.System.console
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [WorkoutContentFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class WorkoutContentFragment : Fragment() {
//    // TODO: Rename and change types of parameters
//
//    private lateinit var workoutAdaptInput: String
//
//    // TODO: adjust max selection logic
//    private var selectedCount = 0  // Track the number of selected workouts
//    private val maxSelections = 3  // Maximum number of workouts user can select
//
//
//    // TODO: text1 currently redundant
//    private lateinit var text1 : TextView
//
//    // TODO: onCreate() currently redundant
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_workout_content, container, false)
//
//        // get WorkoutAdapter.kt input: a stringified list of [exerciseName, muscleGrp, level]
//        workoutAdaptInput = (arguments?.getString("workoutName") ?: 0).toString()
//
//        // change WorkoutAdapter.kt input from a String into a List<String>
//        val exerciseData = workoutAdaptInput.drop(1).dropLast(1).split(", ")
//        val exerciseList = exerciseData.chunked(exerciseData.size / 2).map { it.joinToString(",") }
//
//        // set exercise_name in fragment_workout_content.xml
//        val setExerciseName = view?.findViewById<TextView>(R.id.exercise_name)
//        setExerciseName?.text = exerciseList[0]
//
//        // set muscleGrp in fragment_workout_content.xml
//        val setMuscleGrp = view?.findViewById<TextView>(R.id.muscleGrp)
//        setMuscleGrp?.text = exerciseList[1]
//
//        // set level in fragment_workout_content.xml
//        val setLevel = view?.findViewById<TextView>(R.id.level)
//        setLevel?.text = exerciseList[2]
//
//        // Handle the workout selection logic
//        setExerciseName?.setOnClickListener {
//            if (selectedCount < maxSelections) {
//                // Update the background to grey out the selected workout
//                setExerciseName.setBackgroundColor(resources.getColor(android.R.color.darker_gray))  // Change to grey color
//                selectedCount++
//
//                // If the user selects 3 workouts, disable further selection
//                if (selectedCount >= maxSelections) {
//                    disableAllSelections(view)
//                }
//            }
//        }
//
//        return view
//    }
//
//    // Disable all workout selections (grey out and make unclickable)
//    private fun disableAllSelections(view: View) {
//        val allWorkouts = listOf<TextView>(
//            view.findViewById(R.id.exercise_name),
//            view.findViewById(R.id.muscleGrp),
//            view.findViewById(R.id.level)
//        )
//
//        for (workout in allWorkouts) {
//            workout.isClickable = false
//            workout.setBackgroundColor(resources.getColor(android.R.color.darker_gray))  // Grey out any remaining options
//        }
//    }
//}