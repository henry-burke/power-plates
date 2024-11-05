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

        // TODO - Get shared preferences from using R.string.userPasswdKV as the name
        userPasswdKV = requireActivity().getSharedPreferences(
            getString(R.string.userPasswdKV),
            Context.MODE_PRIVATE)
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
            val user = usernameEditText.text.toString()
            val pass = passwordEditText.text.toString()
            //Log.d("string check", user)
            // TODO: Set the logged-in user in the ViewModel (store user info) (placeholder)
            //userViewModel.setUser(UserState(0, user, pass)) // You will implement this in UserViewModel
            // TODO: Navigate to another fragment after successful login

            if (user.isEmpty() || pass.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Launch a coroutine to call the suspend function
            CoroutineScope(Dispatchers.Main).launch {
                val loginSuccessful = getUserPasswd(user, pass)

                if (loginSuccessful) {
                    // Navigate to another fragment after successful login
                    //userViewModel.setUser(UserState(0, user, pass)) // Assuming you want to store an ID as well

                    userViewModel.setUser(UserState(id, user, pass))
                    findNavController().navigate(R.id.action_loginFragment_to_choiceLevelFragment)
                } else {
                    // Show an error message if login fails
                    errorTextView.visibility = View.VISIBLE
                }
            }

        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        // TODO: Hash the plain password using a secure hashing function
        val hashedInput = hash(passwdPlain)
        // TODO: Check if the user exists in SharedPreferences (using the username as the key)
        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        if(sharedPref?.contains(name) == true){
            val findPassword = sharedPref.getString(name, "")
            return findPassword == hashedInput
        }
        else{
            val editor = sharedPref?.edit()
            //hash password
            editor?.putString(name, hashedInput)
            editor?.apply()



            return true
        }


        // TODO: Retrieve the stored password from SharedPreferences

        // TODO: Compare the hashed password with the stored one and return false if they don't match

        // TODO: If the user doesn't exist in SharedPreferences, create a new user

        // TODO: Insert the new user into the Room database (implement this in your User DAO)

        // TODO: Store the hashed password in SharedPreferences for future logins

        // TODO: Return true if the user login is successful or the user was newly created

        //return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}