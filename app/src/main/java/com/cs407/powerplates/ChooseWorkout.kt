package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.WorkoutType
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ChooseWorkout( private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    private lateinit var greetingTextView: TextView
    private lateinit var workRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var worAdap: WorkoutAdapter
    private lateinit var intermediate: Button
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var itemsArrayList: ArrayList<WorkoutType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //load user view model
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        userLevelKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity())[UserViewModel::class.java]
        }
        userId = userViewModel.userState.value.id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_workout, container, false)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)

        showWorkouts(view)
    }

    private fun showWorkouts(view: View){
        workRecyclerView = view.findViewById(R.id.workoutRecyclerView)
        workRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val workout1 = WorkoutType("a", "a", "a")
        val workout2 = WorkoutType("b", "b", "b")
        val workout3 = WorkoutType("c", "c", "c")
        val workout4 = WorkoutType("d", "d", "d")
        val workout5 = WorkoutType("e", "e", "e")
        val workout6 = WorkoutType("f", "f", "f")
        val workout7 = WorkoutType("g", "g", "g")

        itemsArrayList = arrayListOf()
        itemsArrayList.add(workout1)
        itemsArrayList.add(workout2)
        itemsArrayList.add(workout3)
        itemsArrayList.add(workout4)
        itemsArrayList.add(workout5)
        itemsArrayList.add(workout6)
        itemsArrayList.add(workout7)

        worAdap = WorkoutAdapter(itemsArrayList)
        workRecyclerView.setHasFixedSize(true)
        workRecyclerView.adapter = worAdap

    }

}