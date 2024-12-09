package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.UserState
import com.cs407.powerplates.UserViewModel
import com.cs407.powerplates.data.ExerciseDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChoiceLevel (
    private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {

    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    private lateinit var greetingTextView: TextView
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var beginner: CardView
    private lateinit var intermediate: CardView
    private lateinit var advanced: CardView
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var exerciseDB: ExerciseDatabase

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
            // Use ViewModelProvider to init UserViewModel
            ViewModelProvider(requireActivity())[UserViewModel::class.java]
        }
        userId = userViewModel.userState.value.id
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_choice_level, container, false)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        beginner = view.findViewById(R.id.begButton)
        intermediate = view.findViewById(R.id.interButton)
        advanced = view.findViewById(R.id.advButton)
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())

       // noteRecyclerView = view.findViewById(R.id.noteRecyclerView)
        //fab = view.findViewById(R.id.fab)
        //return view
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)
        val name1 = userState.name.toString() + "_level"
        val useId1 = userState.id

        // setting up the button
        var userExcCount = -1;
        CoroutineScope(Dispatchers.Main).launch {
            userExcCount = exerciseDB.exerciseDao().getAllUserExercisesCount(useId1)
        }

        beginner.setOnClickListener{
            if (userPasswdKV.contains(name1)){
                buttonClicked("beginner")
                if (userExcCount == 15) {
                    // navigate directly to the home page
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_homePage)
                } else {
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_chooseWorkout)
                }
            }
            else {
                val action = ChoiceLevelDirections.actionChoiceLevelFragmentToRankPrefFragment("beginner")
                findNavController().navigate(action)
            }
            //findNavController().navigate(R.id.action_choiceLevelFragment_to_rankPrefFragment)
        }

        intermediate.setOnClickListener{
            Log.d("DEBUG", userPasswdKV.contains(name1).toString())
            if (userPasswdKV.contains(name1)){
                buttonClicked("intermediate")
                if (userExcCount == 15) {
                    // navigate directly to the home page
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_homePage)
                } else {
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_chooseWorkout)
                }
            } else {
                val action = ChoiceLevelDirections.actionChoiceLevelFragmentToRankPrefFragment("intermediate")
                findNavController().navigate(action)
            }

            //findNavController().navigate(R.id.action_choiceLevelFragment_to_rankPrefFragment)
        }
        advanced.setOnClickListener{
            //buttonClicked("advanced")
            if (userPasswdKV.contains(name1)){
                buttonClicked("advanced")
                if (userExcCount == 15) {
                    // navigate directly to the home page
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_homePage)
                } else {
                    findNavController().navigate(R.id.action_choiceLevelFragment_to_chooseWorkout)
                }
            }
            else {
                val action = ChoiceLevelDirections.actionChoiceLevelFragmentToRankPrefFragment("advanced")
                findNavController().navigate(action)
            }
        }
    }

    private fun buttonClicked(level: String){
        // TODO make a new shared preference
        val userState = userViewModel.userState.value
        val name1 = userState.name + "_level"
        val editor = userLevelKV.edit()
        //hash password
        editor?.putString(name1, level)
        editor?.apply()
        Log.d("sharedPref Level", userLevelKV.getString(name1, "").toString())

        // TODO going to the next page
    }


}