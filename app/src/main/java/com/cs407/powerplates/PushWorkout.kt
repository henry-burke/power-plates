package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.Exercise
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.History
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

import com.cs407.powerplates.RepCount
import com.cs407.powerplates.RankPrefs
import java.util.Date
import java.util.Locale

class PushWorkout(
    private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {

    // User ViewModel and SharedPreferences
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var userLevelKV: SharedPreferences
    private var userId: Int = 0

    // UI elements for checkboxes and submit button
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox
    private lateinit var checkBox5: CheckBox
    private lateinit var checkBox6: CheckBox
    private lateinit var checkBox7: CheckBox
    private lateinit var checkBox8: CheckBox
    private lateinit var checkBox9: CheckBox

    // database vars
    private lateinit var exerciseDB: ExerciseDatabase
    private val category = "Push"
    private lateinit var savedWorkouts: List<String>
    private lateinit var savedWorkoutLevels: ArrayList<String>

    //cards
    private lateinit var card1: CardView
    private lateinit var card2: CardView
    private lateinit var card3: CardView

    //Card Text Elements
    private lateinit var card1Text: TextView
    private lateinit var card2Text: TextView
    private lateinit var card3Text: TextView


    // Buttons
    private lateinit var finishButton: Button
    private lateinit var changeExerciseButton: Button

    private lateinit var linearLayout1: LinearLayout
    private lateinit var linearLayout2: LinearLayout
    private lateinit var linearLayout3: LinearLayout

    //make sure checkbox persists for the day
    private lateinit var checkboxPrefs: SharedPreferences
    private val pref_name = "prefs"
    //private val check_box1_state_key = "checkbox1"
    private val last_date_changed_key = "last_changed_date"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load user ViewModel and SharedPreferences
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE
        )
        userLevelKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE
        )
        checkboxPrefs = requireContext().getSharedPreferences(
            pref_name, Context.MODE_PRIVATE
        )

        userViewModel = injectedUserViewModel ?: ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userId = userViewModel.userState.value.id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.checkbox_card, container, false)

        //Initialize Cards
        card1 = view.findViewById(R.id.cardView1)
        card2 = view.findViewById(R.id.cardView2)
        card3 = view.findViewById(R.id.cardView3)

        // Initialize checkboxes
        checkBox1 = view.findViewById(R.id.checkBox1)
        checkBox2 = view.findViewById(R.id.checkBox2)
        checkBox3 = view.findViewById(R.id.checkBox3)
        checkBox4 = view.findViewById(R.id.checkBox4)
        checkBox5 = view.findViewById(R.id.checkBox5)
        checkBox6 = view.findViewById(R.id.checkBox6)
        checkBox7 = view.findViewById(R.id.checkBox7)
        checkBox8 = view.findViewById(R.id.checkBox8)
        checkBox9 = view.findViewById(R.id.checkBox9)

        card1Text = view.findViewById(R.id.card1_text)
        card2Text = view.findViewById(R.id.card2_text)
        card3Text = view.findViewById(R.id.card3_text)

        //Initialize Buttons
        finishButton = view.findViewById(R.id.finishButton)
        changeExerciseButton = view.findViewById(R.id.changeExerciseButton)

        // Initialize Linear Layouts
        linearLayout1 = card1.findViewById(R.id.checkbox_layout1)
        linearLayout2 = card2.findViewById(R.id.checkbox_layout2)
        linearLayout3 = card3.findViewById(R.id.checkbox_layout3)
        linearLayout1.setBackgroundResource(R.drawable.start_border)
        linearLayout2.setBackgroundResource(R.drawable.start_border)
        linearLayout3.setBackgroundResource(R.drawable.start_border)



        // Set up submit button to check if all checkboxes are checked
        /*
        submitButton.setOnClickListener {
            if (areAllCheckboxesChecked()) {
                Toast.makeText(context, "All options selected!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select all options", Toast.LENGTH_SHORT).show()
            }
        }

         */

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupBackNavigation()

        // query db for user selected exercises
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
        val getDao = exerciseDB.exerciseDao()


        CoroutineScope(Dispatchers.IO).launch {
            savedWorkouts = getDao.getSavedWorkoutsByCategory(userId, category)

            savedWorkoutLevels = arrayListOf()

            for (workout in savedWorkouts) {
                savedWorkoutLevels.add(getDao.getLevelFromName(workout))
            }

            CoroutineScope(Dispatchers.Main).launch {


                if(savedWorkouts.isNotEmpty() && savedWorkoutLevels.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch{
                        val lis = exerciseDB.rankedDao().getUserPreferences(userId)

                        val arr = arrayOf(lis.r1, lis.r2, lis.r3, lis.r4, lis.r5 )

                        val massIndex = arr.indexOf("Muscle Mass") + 1
                        val strengthIndex = arr.indexOf("Strength") + 1
                        val staminaIndex = arr.indexOf("Stamina") + 1

                        val userState = userViewModel.userState.value
                        val name1 = userState.name + "_level"
                        val userLevel = userPasswdKV.getString(name1, "").toString()


                        val reps = calculateReps(massIndex, strengthIndex, staminaIndex, userLevel, "push")
                        //Log.d("Crash", userLevel)

                        //get exercise object progType: reps, weights, or time
                        val firstWorkoutProgType = exerciseDB.exerciseDao().getProgTypeFromName("${savedWorkouts[0]}")
                        val secondWorkoutProgType = exerciseDB.exerciseDao().getProgTypeFromName("${savedWorkouts[1]}")
                        val thirdWorkoutProgType = exerciseDB.exerciseDao().getProgTypeFromName("${savedWorkouts[2]}")

                        CoroutineScope(Dispatchers.Main).launch {

                            //check progression type for first workout
                            if (firstWorkoutProgType == "Reps"){
                                card1Text.text = getString(R.string.workout_details_push_to_fail, "${savedWorkouts[0]}", "${savedWorkoutLevels[0]}")
                            }
                            else if(firstWorkoutProgType == "Weight"){
                                card1Text.text = getString(R.string.workout_details, "${savedWorkouts[0]}", "${savedWorkoutLevels[0]}", reps)
                            }
                            else{
                                card1Text.text = getString(R.string.workout_details_no_reps, "${savedWorkouts[0]}", "${savedWorkoutLevels[0]}", timeForWorkout(userLevel,savedWorkouts[0]))
                            }

                            //load last checkbox state for card1
                            workoutCheckBox(checkBox1, userState.name +"checkBox1_"+"${savedWorkouts[0]}")
                            workoutCheckBox(checkBox2, userState.name +"checkBox2_"+"${savedWorkouts[0]}")
                            workoutCheckBox(checkBox3, userState.name +"checkBox3_"+"${savedWorkouts[0]}")



                            //check progression type for second workout
                            if (secondWorkoutProgType == "Reps"){
                                card2Text.text = getString(R.string.workout_details_push_to_fail, "${savedWorkouts[1]}", "${savedWorkoutLevels[1]}")
                            }
                            else if(secondWorkoutProgType == "Weight"){
                                card2Text.text = getString(R.string.workout_details, "${savedWorkouts[1]}", "${savedWorkoutLevels[1]}", reps)
                            }
                            else{
                                card2Text.text = getString(R.string.workout_details_no_reps, "${savedWorkouts[1]}", "${savedWorkoutLevels[1]}", timeForWorkout(userLevel,savedWorkouts[1]))
                            }

                            //load last checkbox state for card2
                            workoutCheckBox(checkBox4, userState.name +"checkBox4_"+"${savedWorkouts[1]}")
                            workoutCheckBox(checkBox5, userState.name +"checkBox5_"+"${savedWorkouts[1]}")
                            workoutCheckBox(checkBox6, userState.name +"checkBox6_"+"${savedWorkouts[1]}")

                            //check progression type for third workout
                            if (thirdWorkoutProgType == "Reps"){
                                card3Text.text = getString(R.string.workout_details_push_to_fail, "${savedWorkouts[2]}", "${savedWorkoutLevels[2]}")
                            }
                            else if(thirdWorkoutProgType == "Weight"){
                                card3Text.text = getString(R.string.workout_details, "${savedWorkouts[2]}", "${savedWorkoutLevels[2]}", reps)
                            }
                            else{
                                card3Text.text = getString(R.string.workout_details_no_reps, "${savedWorkouts[2]}", "${savedWorkoutLevels[2]}", timeForWorkout(userLevel,savedWorkouts[2]))
                            }
                            //load last checkbox state for card3
                            workoutCheckBox(checkBox7, userState.name +"checkBox7_"+"${savedWorkouts[2]}")
                            workoutCheckBox(checkBox8, userState.name +"checkBox8_"+"${savedWorkouts[2]}")
                            workoutCheckBox(checkBox9, userState.name +"checkBox9_"+"${savedWorkouts[2]}")
                        }

                    }
                }
            }
        }

        //card changes color if all text boxes are checked for that card

        checkBox1.setOnCheckedChangeListener { _, _ -> card1AllCheckBoxes() }
        checkBox2.setOnCheckedChangeListener { _, _ -> card1AllCheckBoxes() }
        checkBox3.setOnCheckedChangeListener { _, _ -> card1AllCheckBoxes() }

        checkBox4.setOnCheckedChangeListener { _, _ -> card2AllCheckBoxes() }
        checkBox5.setOnCheckedChangeListener { _, _ -> card2AllCheckBoxes() }
        checkBox6.setOnCheckedChangeListener { _, _ -> card2AllCheckBoxes() }


        checkBox7.setOnCheckedChangeListener { _, _ -> card3AllCheckBoxes() }
        checkBox8.setOnCheckedChangeListener { _, _ -> card3AllCheckBoxes() }
        checkBox9.setOnCheckedChangeListener { _, _ -> card3AllCheckBoxes() }

        //if all checkboxes checked, automatically go back to home page
        //else ask for confirmation that finished with workout
        finishButton.setOnClickListener {
            if (areAllCheckboxesChecked()) {
                Toast.makeText(context, "All options selected!", Toast.LENGTH_SHORT).show()

                // add history to database
                CoroutineScope(Dispatchers.IO).launch {
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    exerciseDB.historyDao().insertExercise(
                        History(userId = userId, category = category, exercise1 = savedWorkouts[0], exercise2 = savedWorkouts[1], exercise3 = savedWorkouts[2], date = formatter.format(
                            Calendar.getInstance().time))
                    )
                }

                findNavController().navigate(R.id.action_pushWorkout_to_homePage)
            } else {
                //Toast.makeText(context, "Please select all options", Toast.LENGTH_SHORT).show()
                unfinishedDialog()
            }
        }

        changeExerciseButton.setOnClickListener {
            val action = PushWorkoutDirections.actionsPushWorkoutToChooseExercise(category, true)
            findNavController().navigate(action)
        }
    }



    private fun card1AllCheckBoxes(){
        if (checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked){
            linearLayout1.setBackgroundResource(R.drawable.selected_card_border)
        }
        else{
            linearLayout1.setBackgroundResource(R.drawable.start_border)
        }
    }

    private fun card2AllCheckBoxes(){
        if (checkBox4.isChecked && checkBox5.isChecked && checkBox6.isChecked){
            linearLayout2.setBackgroundResource(R.drawable.selected_card_border)
        }
        else{
            linearLayout2.setBackgroundResource(R.drawable.start_border)
        }
    }

    private fun card3AllCheckBoxes(){
        if (checkBox7.isChecked && checkBox8.isChecked && checkBox9.isChecked){
            linearLayout3.setBackgroundResource(R.drawable.selected_card_border)
        }
        else{
            linearLayout3.setBackgroundResource(R.drawable.start_border)
        }
    }

    private fun areAllCheckboxesChecked(): Boolean {
        return checkBox1.isChecked &&
                checkBox2.isChecked &&
                checkBox3.isChecked &&
                checkBox4.isChecked &&
                checkBox5.isChecked &&
                checkBox6.isChecked &&
                checkBox7.isChecked &&
                checkBox8.isChecked &&
                checkBox9.isChecked
    }

    private fun unfinishedDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Not Finished")
            .setMessage("You haven't finished all sets. Finish now or go back")
            .setPositiveButton("Finish"){ dialog, _->
                dialog.dismiss()
                findNavController().navigate(R.id.action_pushWorkout_to_homePage)
            }
            .setNegativeButton("Back"){ dialog, _->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun calculateReps(mass: Int, strength: Int, stamina: Int, experienceLevel: String, workoutType: String): String {
        // Safety check if input is valid
        if (mass !in 1..5 || strength !in 1..5 || stamina !in 1..5) {
            return "Rankings must be between 1 and 5."
        }

        if (experienceLevel !in listOf("beginner", "intermediate", "advanced")) {
            return "Experience level must be 'beginner', 'intermediate', or 'advanced'."
        }

        if (workoutType !in listOf("push", "pull", "legs")) {
            return "Workout type must be 'push', 'pull', or 'legs'."
        }

        // Define base reps
        val levels = mapOf(
            "beginner" to mapOf("push" to 10, "pull" to 12, "legs" to 13),
            "intermediate" to mapOf("push" to 10, "pull" to 8, "legs" to 11),
            "advanced" to mapOf("push" to 6, "pull" to 6, "legs" to 9)
        )

        // Overall score
        val combinedScore = mass + strength + stamina

        // Base number of reps
        var baseReps = levels[experienceLevel]?.get(workoutType)

        // Adjust number of reps
        if (baseReps != null) {
            baseReps = when {
                combinedScore >= 13 -> baseReps + 1
                combinedScore < 10 -> baseReps - 1
                else -> baseReps
            }
        }

        // Return the number of reps as a string
        return baseReps.toString()
    }

    private fun timeForWorkout(experienceLevel: String, workoutType: String): String? {
        val timeWorkouts: Map<String, Map<String, String>> = mapOf(
            "beginner" to mapOf(
                "Leg Raise Hold" to "15 seconds each set",
                "Plank" to "30 seconds each set",
                "Side Plank" to "15 seconds each set",
                "Biking" to "5 minutes each set",
                "Cardio Rows" to "5 minutes each set",
                "Elliptical" to "5 minutes each set",
                "Jumping Jacks" to "30 seconds each set",
                "Walking" to "8 minutes each set",
                "Squat Hold" to "20 seconds each set",
                "Wall Sit" to "20 seconds each set",
                "Iron Cross" to "20 seconds each set",
                "Jump Rope" to "30 seconds each set",
                "Mountain Climber" to "25 seconds each set",
                "Running" to "5 minutes each set",
                "Swimming" to "5 minutes each set",
                "Dead Hang" to "10 seconds each set",
                "Stair Stepper" to "5 minutes each set",
                "Single Arm Dead Hang" to "10 seconds each set"
            ),
            "intermediate" to mapOf(
                "Leg Raise Hold" to "30 seconds each set",
                "Plank" to "1 minute each set",
                "Side Plank" to "30 seconds each set",
                "Biking" to "8 minutes each set",
                "Cardio Rows" to "8 minutes each set",
                "Elliptical" to "8 minutes each set",
                "Jumping Jacks" to "1 minute 30 seconds each set",
                "Walking" to "15 minutes each set",
                "Squat Hold" to "45 seconds each set",
                "Wall Sit" to "30 seconds each set",
                "Iron Cross" to "45 seconds each set",
                "Jump Rope" to "1 minute 30 seconds each set",
                "Mountain Climber" to "45 seconds each set",
                "Running" to "8 minutes each set",
                "Swimming" to "8 minutes each set",
                "Dead Hang" to "20 seconds each set",
                "Stair Stepper" to "8 minutes each set",
                "Single Arm Dead Hang" to "25 seconds each set"
            ),
            "advanced" to mapOf(
                "Leg Raise Hold" to "1 minute each set",
                "Plank" to "2 minutes each set",
                "Side Plank" to "1 minute each set",
                "Biking" to "15 minutes each set",
                "Cardio Rows" to "15 minutes each set",
                "Elliptical" to "15 minutes each set",
                "Jumping Jacks" to "4 minutes each set",
                "Walking" to "20 minutes each set",
                "Squat Hold" to "1 minute 30 seconds each set",
                "Wall Sit" to "1 minute each set",
                "Iron Cross" to "1 minute each set",
                "Jump Rope" to "3 minutes each set",
                "Mountain Climber" to "1 minute 30 seconds each set",
                "Running" to "15 minutes each set",
                "Swimming" to "15 minutes each set",
                "Dead Hang" to "50 seconds each set",
                "Stair Stepper" to "12 minutes each set",
                "Single Arm Dead Hang" to "20 seconds each set"
            )
        )
        return timeWorkouts[experienceLevel]?.get(workoutType)
    }


    private fun getCurrentDate():String{
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return date.format(Date())
    }

    private fun saveCheckboxState(isChecked: Boolean, currentDate:String, key:String){
        val editor = checkboxPrefs.edit()
        editor.putBoolean(key, isChecked)
        editor.putString(last_date_changed_key, currentDate)
        editor.apply()
    }

    private fun getCheckboxState(key: String): Boolean{
        return checkboxPrefs.getBoolean(key, false)
    }

    private fun getLastChangeDate(): String?{
        return checkboxPrefs.getString(last_date_changed_key, "")
    }

    private fun workoutCheckBox(check: CheckBox, workout: String){
        //get current date and past date
        val currentDate = getCurrentDate()
        val lastDate = getLastChangeDate()

        val isChecked = getCheckboxState(workout)
        if (lastDate.toString() != currentDate){
            check.isChecked = false

            saveCheckboxState(false, currentDate, workout)

        }
        else{
            check.isChecked = isChecked

            check.setOnCheckedChangeListener { _, _ ->
                // Save checkbox state asynchronously in SharedPreferences
                // Update card color immediately (on the main thread)
                CoroutineScope(Dispatchers.IO).launch {
                    saveCheckboxState(check.isChecked, currentDate, workout)
                }
                updateCardColor()
            }
        }
        //check.setOnCheckedChangeListener { _, _ -> card1AllCheckBoxes() }

        // check.setOnCheckedChangeListener { _, _->  saveCheckboxState(check.isChecked, currentDate, workout) }


    }

    private fun updateCardColor() {
        // Update card colors based on checkbox states immediately
        // Run on the main thread to ensure UI updates are smooth
        CoroutineScope(Dispatchers.Main).launch {
            card1AllCheckBoxesDynamic()
            card2AllCheckBoxesDynamic()
            card3AllCheckBoxesDynamic()
        }
    }

    private fun card1AllCheckBoxesDynamic() {
        // Use CoroutineScope to ensure UI updates happen on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            if (checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked) {
                linearLayout1.setBackgroundResource(R.drawable.selected_card_border)
            } else {
                linearLayout1.setBackgroundResource(R.drawable.start_border)
            }
        }
    }

    private fun card2AllCheckBoxesDynamic() {
        // Use CoroutineScope to ensure UI updates happen on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            if (checkBox4.isChecked && checkBox5.isChecked && checkBox6.isChecked) {
                linearLayout2.setBackgroundResource(R.drawable.selected_card_border)
            } else {
                linearLayout2.setBackgroundResource(R.drawable.start_border)
            }
        }
    }

    private fun card3AllCheckBoxesDynamic() {
        // Use CoroutineScope to ensure UI updates happen on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            if (checkBox7.isChecked && checkBox8.isChecked && checkBox9.isChecked) {
                linearLayout3.setBackgroundResource(R.drawable.selected_card_border)
            } else {
                linearLayout3.setBackgroundResource(R.drawable.start_border)
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }



}