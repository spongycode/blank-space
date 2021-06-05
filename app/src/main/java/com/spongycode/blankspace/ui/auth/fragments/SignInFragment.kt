package com.spongycode.blankspace.ui.auth.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthException
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentSigninBinding
import com.spongycode.blankspace.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInFragment: Fragment() {

    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = AuthActivity().firebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            signInButton.setOnClickListener {
                signInUser(this.emailTextField.editText?.text.toString(), this.passwordTextField.editText?.text.toString())
            }

            signIntoSignUpTV.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }

    private fun signInUser(
        email: String,
        password: String
    ) {
        if(email.isNotBlank() && password.isNotBlank()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    checkSignInState()
                } catch (e: FirebaseAuthException){
                    withContext(Dispatchers.Main){
                        Toast.makeText(requireContext(), "Failed to login, try again", Toast.LENGTH_SHORT).show()
                        Log.w("authloginFailed", e.stackTrace.toString())
                    }
                }
            }
        }
    }

    private fun checkSignInState() {
        if (firebaseAuth.currentUser != null){
            findNavController().navigate(R.id.action_signInFragment_to_mainActivity)
            activity?.finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}