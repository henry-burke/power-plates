package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import com.cs407.powerplates.data.Exercise
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

    // navigation variables
    private lateinit var workRecyclerView: RecyclerView
    private lateinit var worAdap: WorkoutAdapter
    private lateinit var prev: FloatingActionButton
    private lateinit var done: FloatingActionButton
    private var droppedIn = false

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

    // handleWorkoutSelection() variables
    private var exerciseId = -1
    private var isSelected = false
    private var selectedCount = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        prev = view.findViewById(R.id.backButton)

        val args = ChooseWorkoutArgs.fromBundle(requireArguments())
        val inputCategory = args.category
        droppedIn = args.droppedIn

        if(categories.contains(inputCategory)) {
            currCategory = inputCategory
            currentCategoryIndex = categories.indexOf(currCategory)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prev.setOnClickListener{
            moveToPrevCategory(view)
        }

        done.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                // if user has selected 3 workouts, move on to next category
                val selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, categories[currentCategoryIndex])
                if (selectedCount == 3) {
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
                    R.id.history -> {
                        findNavController().navigate(R.id.action_chooseWorkout_to_historyFragment)
                        true
                    }
                    R.id.choosePreff -> {
                        findNavController().navigate(R.id.action_chooseWorkout_to_rankPrefsFragment)
                        true
                    }
                    R.id.chooseLvl -> {
                        findNavController().navigate(R.id.action_chooseWorkout_to_choiceLevelFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

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
            onLongClick = { workoutDetail ->
                descriptionDialog(workoutDetail)
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

            if(exerciseDB.exerciseDao().getUsersSavedExerciseCount(userId) == 15) {
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

    private fun moveToPrevCategory(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            currentCategoryIndex--

            if (currentCategoryIndex >= 0) {
                // Load workouts for the next category
                showWorkouts(view)
            } else {
                // All categories processed, navigate or confirm
                findNavController().navigate(R.id.action_chooseWorkout_to_rankPrefsFragment)
            }
        }
    }

    private fun descriptionDialog(workout: String){
        var ex: Exercise
        var message: String?
        CoroutineScope(Dispatchers.IO).launch {
            ex = exerciseDB.exerciseDao().getExerciseByName(workout)
            CoroutineScope(Dispatchers.Main).launch {
            message = "Primary Muscle: ${ex.primaryMuscle}\n" +
                    "Secondary Muscle: ${ex.secondaryMuscle}\n" +
                    "Compound: ${ex.compound}\n" +
                    "Type: ${ex.type}\n" +
                    "Level: ${ex.level}\n" +
                    "Progression Type: ${ex.progressionType}\n" +
                    "Category: ${ex.category}\n" +
                    "Description: ${ex.description}\n"

                AlertDialog.Builder(requireContext())
                    .setTitle(workout)
                    .setMessage(message)
                    .setPositiveButton("Okay"){ dialog, _->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }
}
