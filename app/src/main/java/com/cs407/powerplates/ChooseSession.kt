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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.cardview.widget.CardView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController

class ChooseSession(private val injectedUserViewModel: UserViewModel? = null // For testing only
): Fragment() {

    // declare variables
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private var userId: Int = 0
    private lateinit var push: CardView
    private lateinit var pull: CardView
    private lateinit var leg: CardView
    private lateinit var cardio: CardView
    private lateinit var abs: CardView
    private lateinit var userLevelKV: SharedPreferences

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
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)


        }
        setupBackNavigation()
        val userState = userViewModel.userState.value

        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.nav_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().popBackStack()
                        true
                    }
                    R.id.action_logout -> {
                        userViewModel.setUser(UserState())
                        findNavController().navigate(R.id.action_chooseSession_to_loginFragment)
                        true
                    }
                    R.id.stopwatch -> {
                        findNavController().navigate(R.id.action_chooseSession_to_StopwatchFragment)
                        true
                    }
                    R.id.history -> {
                        findNavController().navigate(R.id.action_chooseSession_to_historyFragment)
                        true
                    }
                    R.id.choosePreff -> {
                        findNavController().navigate(R.id.action_chooseSession_to_rankPrefsFragment)
                        true
                    }
                    R.id.chooseLvl -> {
                        findNavController().navigate(R.id.action_chooseSession_to_choiceLevelFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        push.setOnClickListener{
            findNavController().navigate(R.id.action_chooseSession_to_pushWorkout)
        }
        pull.setOnClickListener{
            findNavController().navigate(R.id.action_chooseSession_to_pullWorkout)
        }
        leg.setOnClickListener{
            findNavController().navigate(R.id.action_chooseSession_to_legWorkout)
        }
        cardio.setOnClickListener{
            findNavController().navigate(R.id.action_chooseSession_to_cardioWorkout)
        }
        abs.setOnClickListener{
            findNavController().navigate(R.id.action_chooseSession_to_abWorkout)
        }
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