package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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


    // UI elements for other parts of the layout (RecyclerView, FAB, etc.)
    private lateinit var card1Text: TextView
    private lateinit var card2Text: TextView
    private lateinit var card3Text: TextView
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var start: Button

    private lateinit var items: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: Adapter

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

        // Initialize checkboxes
        //checkBox1 = rootView.findViewById(R.id.checkBox)
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

        // Additional setup for RecyclerView, FAB, or other UI components can go here

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // You can use this method to handle any additional logic after the view has been created
        val userState = userViewModel.userState.value

        // Example setup for RecyclerView
        // myAdapter = Adapter(items)  // Assuming Adapter and items are defined
        // recyclerView.adapter = myAdapter

        card1Text.text = getString(R.string.workout_details, "Push Ups", "Advanced", 30)
        card2Text.text = getString(R.string.workout_details, "Arm Curls", "Advanced", 20)
        card3Text.text = getString(R.string.workout_details, "Bicep Curls", "Advanced", 10)
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
}
