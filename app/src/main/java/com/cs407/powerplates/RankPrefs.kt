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

    private lateinit var itemsArrayList: ArrayList<WorkoutData>

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
        fab = view.findViewById(R.id.fab1)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.rank_prefs_greeting)

     
        showPrefs(view)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_rankPrefsFragment_to_chooseWorkout)

        }
    }

    private fun showPrefs(view: View){

        recyclerView = view.findViewById(R.id.recyclerView)

        itemsArrayList = arrayListOf()

        itemsArrayList = arrayListOf(
            WorkoutData("Strength", R.drawable.baseline_fitness_center_24),        // Strength icon
            WorkoutData("Stamina", R.drawable.stamina_icon),        // Stamina icon
            WorkoutData("Muscle Mass", R.drawable.mass_icon),   // Muscle Mass icon
            WorkoutData("Mobility", R.drawable.mobility_icon),      // Mobility icon
            WorkoutData("Body Fat", R.drawable.body_fat_icon)     // Body Fat icon
        )

        myAdapter = Adapter(itemsArrayList)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = myAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.adapterPosition
                val targetPosition = target.adapterPosition

                Collections.swap(itemsArrayList,sourcePosition,targetPosition)
                myAdapter.notifyItemMoved(sourcePosition,targetPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}