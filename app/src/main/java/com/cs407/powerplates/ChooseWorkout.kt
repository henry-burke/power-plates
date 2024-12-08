package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
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
import android.widget.Toast
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
    private val savedWorkoutsByCategory = mutableMapOf<String, ArrayList<String>>()
    private lateinit var currCategory: String
    private lateinit var savedWorkouts: List<String>
    private lateinit var currentSavedWorkouts: ArrayList<String>
    private lateinit var currentSavedCategories: ArrayList<String>

    private var droppedIn = false

    // handleWorkoutSelection() variables
    private var exerciseId = -1
    private var isSelected = false
    private var selectedCount = -1

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

        val args = ChooseWorkoutArgs.fromBundle(requireArguments())
        val inputCategory = args.category
        droppedIn = args.droppedIn
        Log.v("test", "Dropped In: $droppedIn")

        if(categories.contains(inputCategory)) {
            currCategory = inputCategory
            currentCategoryIndex = categories.indexOf(currCategory)
        }

        // TODO: maybe args
//        val inputCategory = (arguments?.getString("workoutName") ?: 0).toString()
//        if (categories.contains(inputCategory)) {
//            currCategory = inputCategory
//            currentCategoryIndex = categories.indexOf(currCategory)
//        }

        // TODO: remove greeting text
        // greetingTextView = view.findViewById(R.id.greetingTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        done.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                // if user has selected 3 workouts, move on to next category
                val selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, categories[currentCategoryIndex])
                if (selectedCount == 3) {

                    Log.v("test", "Dropped In: $droppedIn")

                    CoroutineScope(Dispatchers.Main).launch {
                        val args = ChooseWorkoutArgs.fromBundle(requireArguments())
                        droppedIn = args.droppedIn

                        if(droppedIn) {
                            if (currCategory == "Abs") {
                                findNavController().navigate(R.id.action_chooseWorkout_to_abWorkout)
                            } else if (currCategory == "Cardio") {
                                findNavController().navigate(R.id.action_chooseWorkout_to_cardioWorkout)
                            } else if (currCategory == "Legs") {
                                findNavController().navigate(R.id.action_chooseWorkout_to_legWorkout)
                            } else if (currCategory == "Pull") {
                                findNavController().navigate(R.id.action_chooseWorkout_to_pullWorkout)
                            } else if (currCategory == "Push") {
                                findNavController().navigate(R.id.action_chooseWorkout_to_pushWorkout)
                            }
                        } else {
                            moveToNextCategory(view)
                        }
                    }
                // otherwise, send toast to prompt 3 exercises
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Please select 3 workouts before proceeding.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

    private suspend fun showWorkouts(view: View){
        // init variables
        workRecyclerView = view.findViewById(R.id.workoutRecyclerView)
        workRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
        currCategory = categories[currentCategoryIndex]

        Toast.makeText(context, "Choose 3 workouts for category: $currCategory", Toast.LENGTH_LONG).show()

        // get names, muscles, levels specified by CATEGORY
        nameList = exerciseDB.exerciseDao().getAllNamesByCategory(currCategory)
        muscleList = exerciseDB.exerciseDao().getAllPrimaryMusclesByCategory(currCategory)
        levelList = exerciseDB.exerciseDao().getAllLevelsByCategory(currCategory)

        // put all available exercise of currCategory into exerciseArrayList
        exerciseArrayList = arrayListOf()
        for (i in nameList.indices) {
            exerciseArrayList.add( WorkoutType(nameList[i], muscleList[i], levelList[i], currCategory) )
        }

        // find the currently selected exercises by userId and currCategory
        savedWorkouts = exerciseDB.exerciseDao().getSavedWorkoutsByCategory(userId, currCategory)
        savedWorkoutsByCategory[currCategory] = ArrayList(savedWorkouts.map { it })

        currentSavedWorkouts = savedWorkoutsByCategory[currCategory] ?: arrayListOf()
        currentSavedCategories = ArrayList(currentSavedWorkouts.map { currCategory})

        // initialize the adapter
        worAdap = WorkoutAdapter(
            onClick = { workout ->
                handleWorkoutSelection(workout[0], currCategory, view)
            },
            exerciseArrayList,
            currentSavedWorkouts,
            currentSavedCategories
        )
        workRecyclerView.setHasFixedSize(true)
        workRecyclerView.adapter = worAdap
    }

    private fun handleWorkoutSelection(workoutName: String, currCategory: String, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            exerciseId = exerciseDB.exerciseDao().getIdFromName(workoutName)
            isSelected = exerciseDB.exerciseDao().countUerByUIDandEID(userId, exerciseId) > 0

            if (isSelected) {
                // Remove from database if already selected
                exerciseDB.deleteDao().deleteExerciseFromUERelation(workoutName)
            } else {
                // Add to database if less than 3 selected in this category
                selectedCount = exerciseDB.exerciseDao().getUserExerciseCountByCategory(userId, currCategory)
                if (selectedCount < 3) {
                    exerciseDB.exerciseDao().insertUserExerciseRelation(userId, workoutName)
                }
            }
        }
    }

    private fun moveToNextCategory(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            currentCategoryIndex++

            if(exerciseDB.exerciseDao().getAllUserExercises(userId) == 15) {
                findNavController().navigate(R.id.action_chooseWorkout_to_homePage)
            } else if (currentCategoryIndex < categories.size) {
                // Load workouts for the next category
                showWorkouts(view)
            } else {
                // All categories processed, navigate or confirm
                findNavController().navigate(R.id.action_chooseWorkout_to_homePage)
            }
        }
    }
}


