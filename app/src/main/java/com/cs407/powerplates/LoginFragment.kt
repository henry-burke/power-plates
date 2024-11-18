package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.lab5_milestone.UserState
import com.cs407.lab5_milestone.UserViewModel
import com.cs407.powerplates.R
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.User

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LoginFragment(
    private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView

    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var userLevelKV: SharedPreferences

    private lateinit var exerciseDB: ExerciseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            // TODO - Use ViewModelProvider to init UserViewModel
            ViewModelProvider(requireActivity())[UserViewModel::class.java]
        }
        exerciseDB = ExerciseDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            val dbTest = exerciseDB.exerciseDao().getById(0).toString()
            Log.v("DATABASE TESTING", dbTest)
        }

        // TODO - Get shared preferences from using R.string.userPasswdKV as the name
        this.userPasswdKV = activity?.getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)!!
        this.userLevelKV = activity?.getSharedPreferences(
            getString(R.string.userLevelKV), Context.MODE_PRIVATE)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        usernameEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        passwordEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        // Set the login button click action
        loginButton.setOnClickListener {

            val user = usernameEditText.text.toString()
            val pass = passwordEditText.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Launch a coroutine to call the suspend function
            CoroutineScope(Dispatchers.Main).launch {
                val loginSuccessful = getUserPasswd(user, pass)
                val userCreated = userCreated(user)

                if (userCreated){
                    // TODO go directly to the shared page
                }

                if (loginSuccessful) {
                    // Navigate to another fragment after successful login
                    //userViewModel.setUser(UserState(0, user, pass)) // Assuming you want to store an ID as well
                    val usersid = exerciseDB.userDao().getByName(user).userId
                    userViewModel.setUser(UserState(usersid, user, pass))
                    findNavController().navigate(R.id.action_loginFragment_to_choiceLevelFragment)
                } else {
                    // Show an error message if login fails
                    errorTextView.visibility = View.VISIBLE
                }
            }

        }
    }

    private suspend fun userCreated(user: String): Boolean {
        if (userLevelKV.contains(user)){
            return true;
        }
        else {
            return false
        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        // TODO: Hash the plain password using a secure hashing function
        val hashedInput = hash(passwdPlain)
        // TODO: Check if the user exists in SharedPreferences (using the username as the key)


        if(userPasswdKV.contains(name)){
            val findPassword = userPasswdKV.getString(name, "")
            return findPassword == hashedInput
        }
        else{
            withContext(Dispatchers.IO) {
                exerciseDB.userDao().insert(User(userName = name))
            }

            val editor = userPasswdKV.edit()
            //hash password
            editor?.putString(name, hashedInput)
            editor?.apply()

            return true
        }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}