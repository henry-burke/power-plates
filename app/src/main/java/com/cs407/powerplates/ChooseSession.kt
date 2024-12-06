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
import java.util.Collections


class ChooseSession(private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    private lateinit var greetingTextView: TextView
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var push: Button
    private lateinit var pull: Button
    private lateinit var leg: Button
    private lateinit var cardio: Button
    private lateinit var abs: Button
    private lateinit var userLevelKV: SharedPreferences

    private lateinit var items: ArrayList<String>
    private lateinit var recyclerView: RecyclerView


    private lateinit var itemsArrayList: ArrayList<String>

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
            // TODO - Use ViewModelProvider to init UserViewModel
            ViewModelProvider(requireActivity())[UserViewModel::class.java]
        }
        userId = userViewModel.userState.value.id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_session, container, false)
        push = view.findViewById(R.id.pushButton)
        pull = view.findViewById(R.id.pullButton)
        leg = view.findViewById(R.id.legButton)
        cardio = view.findViewById(R.id.cardioButton)
        abs = view.findViewById(R.id.absButton)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value

        push.setOnClickListener{
            //buttonClicked("beginner")
            findNavController().navigate(R.id.action_chooseSession_to_pushWorkout)
        }
        pull.setOnClickListener{
            //buttonClicked("beginner")
            findNavController().navigate(R.id.action_chooseSession_to_pullWorkout)
        }
        leg.setOnClickListener{
            //buttonClicked("beginner")
            findNavController().navigate(R.id.action_chooseSession_to_legWorkout)
        }
        cardio.setOnClickListener{
            //buttonClicked("beginner")
            findNavController().navigate(R.id.action_chooseSession_to_cardioWorkout)
        }
        abs.setOnClickListener{
            //buttonClicked("beginner")
            findNavController().navigate(R.id.action_chooseSession_to_abWorkout)
        }



    }



}