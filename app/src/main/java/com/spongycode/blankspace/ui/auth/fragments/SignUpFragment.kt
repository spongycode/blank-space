package com.spongycode.blankspace.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.SetOptions
import com.spongycode.blankspace.MainActivity
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentSignupBinding
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.auth.AuthActivity.Companion.usersCollectionReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment: Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    val firebaseAuth = AuthActivity().firebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            signUpButton.setOnClickListener {
                registerUser(
                    this.nameTextField.editText?.text.toString(),
                    this.emailTextField.editText?.text.toString(),
                    this.passwordTextField.editText?.text.toString()
                )
            }

        }
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String
    ){
        if (email.isNotBlank() && password.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    val newUserMap = hashMapOf(
                        "userId" to firebaseAuth.currentUser!!.uid,
                        "name" to name
                    )
                    usersCollectionReference.document(email + name)
                        .set(newUserMap, SetOptions.merge())
                    checkSignUpState()
                } catch (e: FirebaseAuthException){
                    withContext(Dispatchers.Main){
                        Toast.makeText(requireContext(), "Failed to register, try again", Toast.LENGTH_LONG).show()
                        Log.w("authregisterFailed", e.stackTrace.toString())
                    }
                }
            }
        }
    }

    private fun checkSignUpState() {
        if (firebaseAuth.currentUser != null){ // navigate to mainFragment
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}