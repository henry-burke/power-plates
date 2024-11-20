package com.cs407.powerplates

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    private var exName: String = ""
    private lateinit var text1 : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exName = (arguments?.getString("workoutName") ?: 0).toString()
        Log.d("got it", exName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_workout_content, container, false)
        exName = view?.findViewById<TextView>(R.id.Tester).toString()
        return view
    }

}