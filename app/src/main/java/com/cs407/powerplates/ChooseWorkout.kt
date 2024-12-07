package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
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
import androidx.core.view.get
import com.cs407.powerplates.data.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChooseWorkout( private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {
    // login variables
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var userLevelKV: SharedPreferences
    private var userId: Int = 0

    private lateinit var workRecyclerView: RecyclerView
    private lateinit var worAdap: WorkoutAdapter
    private lateinit var done: Button

    // showWorkouts() variables
    private lateinit var exerciseDB: ExerciseDatabase
    private lateinit var nameList: List<String>
    private lateinit var muscleList: List<String>
    private lateinit var levelList: List<String>
    private lateinit var exerciseArrayList: ArrayList<WorkoutType>
    private val categories = listOf("Push", "Pull", "Legs", "Cardio", "Abs")
    private var currentCategoryIndex = 0

    // currently unused variables
    private lateinit var greetingTextView: TextView
    private lateinit var workoutName: String
    private lateinit var intermediate: Button
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: remove deprecated function?
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

        // TODO: remove greeting text
        // greetingTextView = view.findViewById(R.id.greetingTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        done.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                val selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, categories[currentCategoryIndex])
                if (selectedCount == 3) {
                    moveToNextCategory(view)
                } else {
                    Log.i("Selection", "Please select 3 workouts before proceeding")
                }
            }

            // TODO: IMPLEMENT ORIGINAL DONE ONCLICK
//            Log.d("clicked", "pressed")
//            findNavController().navigate(R.id.action_chooseWorkout_to_homePage)
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
                    R.id.stopwatch -> {
                        findNavController().navigate(R.id.action_chooseWorkout_to_StopwatchFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        // TODO: remove greetingText?
        // val userState = userViewModel.userState.value
        // showWorkouts(view)
        // greetingTextView.text = getString(R.string.greeting_text, userState.name)

        // call showWorkouts from coroutine to query from db
        CoroutineScope(Dispatchers.Main).launch {
            showWorkouts(view)
        }
    }

    private suspend fun showWorkouts(view: View) {
        workRecyclerView = view.findViewById(R.id.workoutRecyclerView)
        workRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())

        val currentCategory = categories[currentCategoryIndex]
        Log.i("Category", "Loading workouts for category: $currentCategory")

        // Query the database for workouts in the current category
        nameList = exerciseDB.exerciseDao().getAllNamesByCategory(currentCategory)
        muscleList = exerciseDB.exerciseDao().getAllPrimaryMusclesByCategory(currentCategory)
        levelList = exerciseDB.exerciseDao().getAllLevelsByCategory(currentCategory)

        exerciseArrayList = arrayListOf()

        // Populate the workout list for the current category
        for (i in nameList.indices) {
            exerciseArrayList.add(WorkoutType(nameList[i], muscleList[i], levelList[i]))
        }

        // Find already selected exercises for the user
        val userExerciseArrList = arrayListOf<String>()
        val userExerciseList = exerciseDB.exerciseDao().getUerExercisesByUID(userId)
        for (exercise in userExerciseList) {
            userExerciseArrList.add(exercise)
        }

        // Initialize the adapter
        worAdap = WorkoutAdapter(
            onClick = { workout ->
                handleWorkoutSelection(workout[0], currentCategory, view)
            },
            exerciseArrayList,
            userExerciseArrList
        )
        workRecyclerView.setHasFixedSize(true)
        workRecyclerView.adapter = worAdap
    }


//    private suspend fun showWorkouts(view: View){
//        workRecyclerView = view.findViewById(R.id.workoutRecyclerView)
//        workRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
//
//        // TODO: get names, muscles, levels specified by CATEGORY
//        nameList = exerciseDB.exerciseDao().getAllNames()
//        muscleList = exerciseDB.exerciseDao().getAllPrimaryMuscles()
//        levelList = exerciseDB.exerciseDao().getAllLevels()
//
//        exerciseArrayList = arrayListOf()
//
//        // TODO: only add a specific category of workouts
//        for (i in nameList.indices) {
//            exerciseArrayList.add( WorkoutType(nameList[i], muscleList[i], levelList[i]) )
//        }
//
//        // find the currently selected exercises by userId
//        val userExerciseArrList = arrayListOf<String>()
//        val userExerciseList = exerciseDB.exerciseDao().getUerExercisesByUID(userId)
//        for (exercise in userExerciseList) {
//            userExerciseArrList.add(exercise)
//        }
//        Log.i("userExerciseList", userExerciseList.toString())
//
//        var selectedCount = 0
//        var currCategory = "Abs"
//
//        // category: Abs
//        var e1 = "Lying Leg Raise"
//        var e2 = "Oblique Twist"
//        var e3 = "Bicycle Crunch"
//        var e4 = "Crunches"
//
//        // category: Cardio
//        var e5 = "Biking"
//
//        // TODO; NEED offsets for different categories
//
//        worAdap = WorkoutAdapter(
//            onClick = { workout ->
//                var exerciseName = workout[0]
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    // exerciseId of most recently clicked exercise
//                    val exerciseId = exerciseDB.exerciseDao().getIdFromName(exerciseName)
//
//                    // check if exerciseName is already a UER for userId
//                    val isSelected = exerciseDB.exerciseDao().countUerByUIDandEID(userId, exerciseId) > 0
//
//                    // remove clicked exercise if already selected by user
//                    if (isSelected) {
//                        exerciseDB.deleteDao().deleteExerciseFromUERelation(exerciseName)
//                    // otherwise check if we already have 3 selected exercises
//                    } else {
//                        selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, currCategory)
//                        if (selectedCount < 3) {
//                            exerciseDB.exerciseDao().insertUserExerciseRelation(userId, exerciseName)
//                        }
//                    }
//                }
////                val action = ChooseWorkoutDirections.actionChooseWorkoutToWorkoutContentFragment(
////                    exerciseName.toString()
////                )
////                findNavController().navigate(action)
//            },
//            exerciseArrayList,
//            userExerciseArrList
//        )
//        workRecyclerView.setHasFixedSize(true)
//        workRecyclerView.adapter = worAdap
//    }

    private fun handleWorkoutSelection(workoutName: String, currentCategory: String, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            val exerciseId = exerciseDB.exerciseDao().getIdFromName(workoutName)
            val isSelected = exerciseDB.exerciseDao().countUerByUIDandEID(userId, exerciseId) > 0

            if (isSelected) {
                // Remove from database if already selected
                exerciseDB.deleteDao().deleteExerciseFromUERelation(workoutName)
            } else {
                // Add to database if less than 3 selected in this category
                val selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, currentCategory)
                if (selectedCount < 3) {
                    exerciseDB.exerciseDao().insertUserExerciseRelation(userId, workoutName)
                }
            }

            // Check if the user has selected 3 exercises
            val updatedCount = exerciseDB.exerciseDao().userExerciseCount(userId, currentCategory)
            if (updatedCount == 3) {
                // Move to the next category
                moveToNextCategory(view)
            }
        }
    }


    private fun moveToNextCategory(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            currentCategoryIndex++

            if (currentCategoryIndex < categories.size) {
                // Load workouts for the next category
                showWorkouts(view)
            } else {
                // All categories processed, navigate or confirm
                Log.i("Workflow", "All categories completed")
                findNavController().navigate(R.id.action_chooseWorkout_to_homePage)
            }
        }
    }
}


