package com.cs407.powerplates

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cs407.lab5_milestone.UserState
import com.cs407.lab5_milestone.UserViewModel
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
    //private lateinit var noteDB: NoteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = (if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            // TODO - Use ViewModelProvider to init UserViewModel
            // LEARN and CLEAR all of this section about view modals and what is going on
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        })
        //noteDB = NoteDatabase.getDatabase(requireContext())

        // TODO - Get shared preferences from using R.string.userPasswdKV as the name
        // The thing !! does something check if it is necessary
        this.userPasswdKV = activity?.getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)!!
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
            // TODO: Get the entered username and password from EditText fields
            var userName = usernameEditText.text.toString()
            var password = passwordEditText.text.toString()
            CoroutineScope(Dispatchers.Default).launch{
                if (getUserPasswd(userName, password)){
                    // TODO: Set the logged-in user in the ViewModel (store user info) (placeholder)
                    //var usersid = noteDB.userDao().getByName(userName).userId
                    //userViewModel.setUser(UserState(usersid, userName, password)) // You will implement this in UserViewModel
                    withContext(Dispatchers.Main){
                        Log.i("info", "$userName")
                        // TODO: Navigate to another fragment after successful login
                        //findNavController().navigate(R.id.action_loginFragment_to_noteListFragment)
                    }

                } else {
                    // TODO: Show an error message if either username or password is empty
                    withContext(Dispatchers.Main){
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.text = "Wrong Username or Password "
                    }
                }
            }

        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        // TODO: Hash the plain password using a secure hashing function
        var pass = hash(passwdPlain)

        // TODO: Check if the user exists in SharedPreferences (using the username as the key
        if (userPasswdKV.contains(name)){
            // TODO: Retrieve the stored password from SharedPreferences
            val passwordStored = userPasswdKV.getString(name, null)

            // TODO: Compare the hashed password with the stored one and return false if they don't match
            if (pass != passwordStored) {
                return false
            }
        } // TODO: If the user doesn't exist in SharedPreferences, create a new user
        else {
            // TODO: Store the hashed password in SharedPreferences for future logins
            with(userPasswdKV.edit()){
                putString(name, pass)
                apply()
            }
        }

        // TODO: Return true if the user login is successful or the user was newly created
        return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}