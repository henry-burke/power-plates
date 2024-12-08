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


    //Finish Button
    private lateinit var finishButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load user ViewModel and SharedPreferences
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE
        )
        userLevelKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE
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

        //Initialize Finish Button
        finishButton = view.findViewById(R.id.finishButton)



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

                        CoroutineScope(Dispatchers.Main).launch {
                            card1Text.text = getString(R.string.workout_details, "${savedWorkouts[0]}", "${savedWorkoutLevels[0]}", reps)
                            card2Text.text = getString(R.string.workout_details, "${savedWorkouts[1]}", "${savedWorkoutLevels[1]}", reps)
                            card3Text.text = getString(R.string.workout_details, "${savedWorkouts[2]}", "${savedWorkoutLevels[2]}", reps)
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
    }



    private fun card1AllCheckBoxes(){
        if (checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked){
            card1.setCardBackgroundColor(Color.argb(255, 50, 205,50))
        }
        else{
            card1.setCardBackgroundColor(Color.argb(255, 255, 0,0))
        }
    }

    private fun card2AllCheckBoxes(){
        if (checkBox4.isChecked && checkBox5.isChecked && checkBox6.isChecked){
            card2.setCardBackgroundColor(Color.argb(255, 50, 205,50))
        }
        else{
            card2.setCardBackgroundColor(Color.argb(255, 255, 0,0))
        }
    }

    private fun card3AllCheckBoxes(){
        if (checkBox7.isChecked && checkBox8.isChecked && checkBox9.isChecked){
            card3.setCardBackgroundColor(Color.argb(255, 50, 205,50))
        }
        else{
            card3.setCardBackgroundColor(Color.argb(255, 255, 0,0))
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
            "advanced'" to mapOf("push" to 6, "pull" to 6, "legs" to 9)
        )

        // Overall score
        val combinedScore = mass + strength + stamina

        // Base number of reps
        var baseReps = 0

        if (experienceLevel == "beginner" || experienceLevel == "intermediate"){
            baseReps = 10
        }
        else{
            baseReps = 6
        }

        // Adjust number of reps
        baseReps = when {
            combinedScore >= 13 -> baseReps + 1
            combinedScore < 10 -> baseReps - 1
            else -> baseReps
        }

        // Return the number of reps as a string
        return baseReps.toString()
    }


}
