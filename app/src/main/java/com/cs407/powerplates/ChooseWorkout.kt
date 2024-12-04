package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.WorkoutType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider


class ChooseWorkout( private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    //private lateinit var greetingTextView: TextView
    private lateinit var workRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var worAdap: WorkoutAdapter
    private lateinit var done: Button
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var itemsArrayList: ArrayList<WorkoutType>
    private lateinit var workoutName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setHasOptionsMenu(true)

        //load user view model
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        userLevelKV = requireContext().getSharedPreferences(
            getString(R.string.userLevelKV), Context.MODE_PRIVATE)

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
        done = view.findViewById(R.id.done)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        done.setOnClickListener{
            Log.d("clicked", "pressed")
            findNavController().navigate(R.id.action_chooseWorkout_to_homePage)
        }


        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.nav_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        userViewModel.setUser(UserState())
                        findNavController().navigate(R.id.action_chooseWorkout_to_loginFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        val userState = userViewModel.userState.value
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

        workoutName = "abs"

        worAdap = WorkoutAdapter(
            onClick = { workoutName ->
                //val action = ChooseWorkoutDirections.actionChooseWorkoutToWorkoutContentFragment(workoutName)
                //findNavController().navigate(action)
            },
            itemsArrayList
        )
        workRecyclerView.setHasFixedSize(true)
        workRecyclerView.adapter = worAdap
    }

}