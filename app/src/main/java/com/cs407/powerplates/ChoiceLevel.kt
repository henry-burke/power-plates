package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cs407.powerplates.data.ExerciseDatabase
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity).supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(false) // Hide the back button
        }

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)
        val name1 = userState.name.toString() + "_level"
        val useId1 = userState.id

        // setting up the button
        var userExcCount = -1;
        CoroutineScope(Dispatchers.Main).launch {
            userExcCount = exerciseDB.exerciseDao().getUsersSavedExerciseCount(useId1)
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
        }
        advanced.setOnClickListener{
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
        val userState = userViewModel.userState.value
        val name1 = userState.name + "_level"
        val editor = userLevelKV.edit()
        //hash password
        editor?.putString(name1, level)
        editor?.apply()
    }
}
