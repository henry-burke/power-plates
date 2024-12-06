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
        workRecyclerView = view.findViewById(R.id.workoutRecyclerView)
        workRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        exerciseDB = ExerciseDatabase.getDatabase(requireContext())

        nameList = exerciseDB.exerciseDao().getAllNames()
        muscleList = exerciseDB.exerciseDao().getAllPrimaryMuscles()
        levelList = exerciseDB.exerciseDao().getAllLevels()

        exerciseArrayList = arrayListOf()

        // TODO: only add a specific category of workouts
        for (i in nameList.indices) {
            exerciseArrayList.add( WorkoutType(nameList[i], muscleList[i], levelList[i]) )
        }

        // find the currently selected exercises by userId
        val userExerciseList = exerciseDB.exerciseDao().getUerExercisesByUID(userId)
        Log.v("TESTINGTESTING", userExerciseList.toString())

        val testArr = arrayListOf<WorkoutType>()

        // TODO: persist selected userExerciseRelations
        for(exercise in userExerciseList) {
            testArr.add(WorkoutType(exercise.exerciseName, exercise.primaryMuscle, exercise.level))
        }




        var selectedCount = 0
        var currCategory = "Abs"

        // category: Abs
        var e1 = "Lying Leg Raise"
        var e2 = "Oblique Twist"
        var e3 = "Bicycle Crunch"
        var e4 = "Crunches"

        // category: Cardio
        var e5 = "Biking"

        // TODO; NEED offsets for different categories

        worAdap = WorkoutAdapter(
            onClick = { exerciseName ->
                // TODO: IMPLEMENT THREE SELECT LOGIC HERE
                CoroutineScope(Dispatchers.IO).launch {
                    // see number of exercises chosen by user
                    selectedCount = exerciseDB.exerciseDao().userExerciseCount(userId, currCategory)

                    // exerciseId of most recently clicked exercise
                    val exerciseId = exerciseDB.exerciseDao().getIdFromName(exerciseName[0])

                    // check if exerciseName[0] is already a UER for userId
                    val checker = exerciseDB.exerciseDao().countUerByUIDandEID(userId, exerciseId)

                    // insert and grey out chosen exercise if room and not already chosen
                    if (selectedCount < 3 && checker == 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            exerciseDB.exerciseDao().insertUserExerciseRelation(userId, exerciseName[0])
                            workRecyclerView[exerciseId - 1].setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        }
                    }
                }
//                val action = ChooseWorkoutDirections.actionChooseWorkoutToWorkoutContentFragment(
//                    exerciseName.toString()
//                )
//                findNavController().navigate(action)
            },
            exerciseArrayList,
            testArr
        )
        workRecyclerView.setHasFixedSize(true)
        workRecyclerView.adapter = worAdap
    }
}