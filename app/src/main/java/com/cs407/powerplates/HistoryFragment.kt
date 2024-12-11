package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.History
import com.cs407.powerplates.data.HistoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment(private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {

    // declare variables
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private var userId: Int = 0
    private lateinit var userLevelKV: SharedPreferences
    private lateinit var exerciseDB: ExerciseDatabase
    private lateinit var dao: HistoryDao
    private lateinit var userHistory: List<History>
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

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
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())
        dao = exerciseDB.historyDao()

        // get current user's history
        CoroutineScope(Dispatchers.IO).launch {
            userHistory = dao.getAllHistoryByUID(userId)

            historyAdapter = HistoryAdapter(
                userHistory
            )
            withContext(Dispatchers.Main) {
                historyRecyclerView.setHasFixedSize(true)
                historyRecyclerView.adapter = historyAdapter
            }
        }
        return view
    }
}