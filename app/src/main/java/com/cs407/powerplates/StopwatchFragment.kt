package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class StopwatchFragment(private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences

    private var userId: Int = 0

    private lateinit var greetingTextView: TextView
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var start: Button
    private lateinit var push: Button
    private lateinit var pull: Button
    private lateinit var leg: Button
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var chronometer: Chronometer
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var lapButton: Button
    private lateinit var lapList: RecyclerView

    private var isRunning = false
    private var elapsedTime = 0L
    private var startTime = 0L
    private val laps = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var millisecondsText: TextView

    private lateinit var lapAdapter: LapAdapter


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
        val view = inflater.inflate(R.layout.fragment_stopwatch, container, false)

        // Initialize views
        chronometer = view.findViewById(R.id.chronometer)
        startPauseButton = view.findViewById(R.id.startPauseButton)
        resetButton = view.findViewById(R.id.resetButton)
        lapButton = view.findViewById(R.id.lapButton)
        millisecondsText = view.findViewById(R.id.millisecondsText)
        lapList = view.findViewById(R.id.lapList)

        lapAdapter = LapAdapter(laps)
        lapList.layoutManager = LinearLayoutManager(requireContext())
        lapList.adapter = lapAdapter

        // Start/Pause button logic
        startPauseButton.setOnClickListener {
            if (isRunning) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime
                chronometer.stop()
                handler.removeCallbacks(updateMilliseconds)
                startPauseButton.text = "Resume"
            } else {
                startTime = SystemClock.elapsedRealtime() - elapsedTime
                chronometer.base = startTime
                chronometer.start()
                handler.post(updateMilliseconds)
                startPauseButton.text = "Pause"
            }
            isRunning = !isRunning
        }

        // Reset button logic
        resetButton.setOnClickListener {
            chronometer.stop()
            handler.removeCallbacks(updateMilliseconds)
            elapsedTime = 0L
            chronometer.base = SystemClock.elapsedRealtime()
            millisecondsText.text = "000"
            startPauseButton.text = "Start"
            isRunning = false
            laps.clear()
            lapAdapter.notifyDataSetChanged()
        }

        // Lap button logic
        lapButton.setOnClickListener {
            if (isRunning) {
                val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
                val minutes = elapsedMillis / 60000
                val seconds = (elapsedMillis % 60000) / 1000
                val milliseconds = (elapsedMillis % 1000)
                laps.add(
                    String.format("Lap %d: %02d:%02d.%03d", laps.size + 1, minutes, seconds, milliseconds)
                )
                lapAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    private val updateMilliseconds = object : Runnable {
        override fun run() {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            val milliseconds = (elapsedMillis % 1000)
            millisecondsText.text = String.format("%03d", milliseconds)
            handler.postDelayed(this, 10) // Update every 10ms for accuracy
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupBackNavigation()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
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
                        findNavController().navigate(R.id.actions_stopwatch_to_Logout)
                        true
                    }
                    R.id.history -> {
                        findNavController().navigate(R.id.actions_stopwatch_to_history)
                        true
                    }
                    R.id.choosePreff -> {
                        findNavController().navigate(R.id.actions_stopwatch_to_rankPrefs)
                        true
                    }
                    R.id.chooseLvl -> {
                        findNavController().navigate(R.id.actions_stopwatch_to_chooseLevel)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)


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