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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.ItemTouchHelper
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.RankedPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections


class RankPrefs(private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    private lateinit var greetingTextView: TextView
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var beginner: Button
    private lateinit var intermediate: Button
    private lateinit var userLevelKV: SharedPreferences

    private lateinit var items: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: Adapter
    private lateinit var userLev: String
    private lateinit var exerciseDB: ExerciseDatabase

    private lateinit var itemsArrayList: ArrayList<WorkoutData>
    private lateinit var userPrefs: RankedPrefs

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
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rank_prefs, container, false)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        userLev = (arguments?.getString("level") ?: 0).toString()
        fab = view.findViewById(R.id.fab1)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.rank_prefs_greeting)

     
        showPrefs(view)

        // TODO: fix merges URGENT
        fab.setOnClickListener {
            buttonClicked(userLev)
            Log.d("inside rank pref", userLev)
            val usersId = userState.id

            exerciseDB = ExerciseDatabase.getDatabase(requireContext())

            // Push correctly
            CoroutineScope(Dispatchers.Main).launch {
                if(exerciseDB.exerciseDao().getUsersSavedExerciseCount(usersId) == 15){
                    // navigate directly to the home page
                    Log.v("test", "going to home page ALERT")
                    savePrefs()
                    findNavController().navigate(R.id.action_rankPrefsFragment_to_homePage)
                } else {
                    // navigate to the exercise choice page
                    Log.v("test", "going to choose workout ALERT")
                    savePrefs()
                    findNavController().navigate(R.id.action_rankPrefsFragment_to_chooseWorkout)
                }
            }

        }
    }

    private fun savePrefs() {
        Log.v("test", "savePrefs ALERT")
        // get current order of user's ranked preferences
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
        val prefsToInsert = RankedPrefs(userId, itemsArrayList[0].title, itemsArrayList[1].title,
            itemsArrayList[2].title, itemsArrayList[3].title, itemsArrayList[4].title)

        // insert user's ranked preferences into database
        CoroutineScope(Dispatchers.IO).launch {
            if(exerciseDB.rankedDao().getUserPreferences(userId) == null) {
                exerciseDB.rankedDao().insertPrefs(prefsToInsert)
            } else {
                exerciseDB.deleteDao().removeUserPreferences(userId)
                exerciseDB.rankedDao().insertPrefs(prefsToInsert)
            }
        }
    }

    private fun showPrefs(view: View) {
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            userPrefs = exerciseDB.rankedDao().getUserPreferences(userId)

        CoroutineScope(Dispatchers.Main).launch {
            recyclerView = view.findViewById(R.id.recyclerView)

            itemsArrayList = arrayListOf()

            if (!::userPrefs.isInitialized) {
                itemsArrayList = arrayListOf(
                    WorkoutData(
                        "Strength",
                        R.drawable.baseline_fitness_center_24
                    ),        // Strength icon
                    WorkoutData("Stamina", R.drawable.stamina_icon),        // Stamina icon
                    WorkoutData("Muscle Mass", R.drawable.mass_icon),   // Muscle Mass icon
                    WorkoutData("Mobility", R.drawable.mobility_icon),      // Mobility icon
                    WorkoutData("Body Fat", R.drawable.body_fat_icon)     // Body Fat icon
                )
            } else {
                val iconMap = mapOf(
                    "Strength" to R.drawable.baseline_fitness_center_24,
                    "Stamina" to R.drawable.stamina_icon, "Muscle Mass" to R.drawable.mass_icon,
                    "Mobility" to R.drawable.mobility_icon, "Body Fat" to R.drawable.body_fat_icon
                )

                itemsArrayList = arrayListOf(
                    WorkoutData(
                        userPrefs.r1,
                        iconMap[userPrefs.r1] ?: R.drawable.baseline_fitness_center_24
                    ),
                    WorkoutData(
                        userPrefs.r2,
                        iconMap[userPrefs.r2] ?: R.drawable.baseline_fitness_center_24
                    ),
                    WorkoutData(
                        userPrefs.r3,
                        iconMap[userPrefs.r3] ?: R.drawable.baseline_fitness_center_24
                    ),
                    WorkoutData(
                        userPrefs.r4,
                        iconMap[userPrefs.r4] ?: R.drawable.baseline_fitness_center_24
                    ),
                    WorkoutData(
                        userPrefs.r5,
                        iconMap[userPrefs.r5] ?: R.drawable.baseline_fitness_center_24
                    ),
                )
            }


            myAdapter = Adapter(itemsArrayList)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = myAdapter


        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.adapterPosition
                val targetPosition = target.adapterPosition

                Collections.swap(itemsArrayList, sourcePosition, targetPosition)
                myAdapter.notifyItemMoved(sourcePosition, targetPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
        }
    }

    private fun buttonClicked(level: String){
        // TODO make a new shared preference
        val userState = userViewModel.userState.value
        val name1 = userState.name + "_level"
        Log.d("test", name1)
        val editor = userLevelKV.edit()
        //hash password
        editor?.putString(name1, level)
        editor?.apply()
        Log.d("sharedPref Level", userLevelKV.getString(name1, "").toString())

        // TODO going to the next page
    }
}