package com.cs407.powerplates

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cs407.powerplates.data.Exercise
import com.cs407.powerplates.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.System.console

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WorkoutContentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WorkoutContentFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var workoutAdaptInput: String

    // TODO: text1 currently redundant
    private lateinit var text1 : TextView

    // TODO: onCreate() currently redundant
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_workout_content, container, false)

        // get WorkoutAdapter.kt input: a stringified list of [exerciseName, muscleGrp, level]
        workoutAdaptInput = (arguments?.getString("workoutName") ?: 0).toString()

        // change WorkoutAdapter.kt input from a String into a List<String>
        val exerciseData = workoutAdaptInput.drop(1).dropLast(1).split(", ")
        val exerciseList = exerciseData.chunked(exerciseData.size / 2).map { it.joinToString(",") }

        // set exercise_name in fragment_workout_content.xml
        val setExerciseName = view?.findViewById<TextView>(R.id.exercise_name)
        setExerciseName?.text = exerciseList[0]

        // set muscleGrp in fragment_workout_content.xml
        val setMuscleGrp = view?.findViewById<TextView>(R.id.muscleGrp)
        setMuscleGrp?.text = exerciseList[1]

        // set level in fragment_workout_content.xml
        val setLevel = view?.findViewById<TextView>(R.id.level)
        setLevel?.text = exerciseList[2]

        return view
    }

}