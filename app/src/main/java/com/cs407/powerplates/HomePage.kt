package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.ExerciseDao
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.History
import com.cs407.powerplates.data.HistoryDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomePage(private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {

    // declare variables
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private var userId: Int = 0
    private lateinit var start: CardView
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var exerciseDB: ExerciseDatabase
    private lateinit var historyDao: HistoryDao
    private lateinit var countArr: ArrayList<Int>
    private lateinit var topExerciseArr: ArrayList<String>
    private val categories = listOf("Push", "Pull", "Legs", "Cardio", "Abs")

    private lateinit var pushCategory: TextView
    private lateinit var pullCategory: TextView
    private lateinit var legsCategory: TextView
    private lateinit var cardioCategory: TextView
    private lateinit var absCategory: TextView

    private lateinit var pushNumWorkouts: TextView
    private lateinit var pullNumWorkouts: TextView
    private lateinit var legsNumWorkouts: TextView
    private lateinit var cardioNumWorkouts: TextView
    private lateinit var absNumWorkouts: TextView

    private lateinit var pushTopExercise: TextView
    private lateinit var pullTopExercise: TextView
    private lateinit var legsTopExercise: TextView
    private lateinit var cardioTopExercise: TextView
    private lateinit var absTopExercise: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // load user view model
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        userLevelKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        // use ViewModelProvider to init UserViewModel
        userViewModel = injectedUserViewModel ?: ViewModelProvider(requireActivity())[UserViewModel::class.java]

        // find current user's ID
        userId = userViewModel.userState.value.id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // init variables
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)
        start = view.findViewById(R.id.startButton)
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
        historyDao = exerciseDB.historyDao()

        pushCategory = view.findViewById(R.id.push_category)
        pullCategory = view.findViewById(R.id.pull_category)
        legsCategory = view.findViewById(R.id.legs_category)
        cardioCategory = view.findViewById(R.id.cardio_category)
        absCategory = view.findViewById(R.id.abs_category)

        pushNumWorkouts = view.findViewById(R.id.push_num_workouts)
        pullNumWorkouts = view.findViewById(R.id.pull_num_workouts)
        legsNumWorkouts = view.findViewById(R.id.legs_num_workouts)
        cardioNumWorkouts = view.findViewById(R.id.cardio_num_workouts)
        absNumWorkouts = view.findViewById(R.id.abs_num_workouts)

        pushTopExercise = view.findViewById(R.id.push_top_exercise)
        pullTopExercise = view.findViewById(R.id.pull_top_exercise)
        legsTopExercise = view.findViewById(R.id.legs_top_exercise)
        cardioTopExercise = view.findViewById(R.id.cardio_top_exercise)
        absTopExercise = view.findViewById(R.id.abs_top_exercise)

        CoroutineScope(Dispatchers.IO).launch {
            countArr = arrayListOf()
            topExerciseArr = arrayListOf()
            for(category in categories) {
                countArr.add( historyDao.getExerciseCountByCategory(userId, category) )

                val currExercise = historyDao.getTopExercisesByCategory(userId, category)

                if(currExercise.isNotEmpty()) {
                    topExerciseArr.add( historyDao.getTopExercisesByCategory(userId, category)[0].exercises )
                } else {
                    topExerciseArr.add("N/A")
                }
            }
            // Switch to the main thread for UI updates
            withContext(Dispatchers.Main) {
                pushCategory.text = categories[0]
                pushNumWorkouts.text = "${countArr[0]}"
                pushTopExercise.text = topExerciseArr[0]

                pullCategory.text = categories[1]
                pullNumWorkouts.text = "${countArr[1]}"
                pullTopExercise.text = topExerciseArr[1]

                legsCategory.text = categories[2]
                legsNumWorkouts.text = "${countArr[2]}"
                legsTopExercise.text = topExerciseArr[2]

                cardioCategory.text = categories[3]
                cardioNumWorkouts.text = "${countArr[3]}"
                cardioTopExercise.text = topExerciseArr[3]

                absCategory.text = categories[4]
                absNumWorkouts.text = "${countArr[4]}"
                absTopExercise.text = topExerciseArr[4]
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value

        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.nav_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        userViewModel.setUser(UserState())
                        findNavController().navigate(R.id.action_homePage_to_loginFragment)
                        true
                    }
                    R.id.stopwatch -> {
                        findNavController().navigate(R.id.action_homePage_to_StopwatchFragment)
                        true
                    }
                    R.id.choosePreff -> {
                        findNavController().navigate(R.id.action_homePage_to_rankPrefsFragment)
                        true
                    }
                    R.id.chooseLvl -> {
                        findNavController().navigate(R.id.action_homePage_to_choiceLevelFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        // setting up the button
        start.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_chooseSession)
        }
    }
}