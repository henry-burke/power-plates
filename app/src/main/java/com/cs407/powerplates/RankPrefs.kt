package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


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
    private lateinit var advanced: Button
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
            // TODO - Use ViewModelProvider to init UserViewModel
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)


    }


}