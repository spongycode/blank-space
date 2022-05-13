package com.spongycode.blankspace.ui.auth.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentSigninBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.NetworkCheck.hasInternetConnection
import com.spongycode.blankspace.util.userdata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInFragment: Fragment() {

    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = AuthActivity().firebaseAuth
    private val firestore = Firebase.firestore



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        Helper.buttonEffect(binding.signInButton, "#C665F37D")
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
            requireActivity().onBackPressedDispatcher.addCallback {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            resetPasswordInit.setOnClickListener {
                ResetPasswordDialog.newInstance().show(parentFragmentManager, "reset")

            }
        }
    }

    private fun signInUser(
        email: String,
        password: String
    ) {
        try {
            if(hasInternetConnection((activity as AuthActivity).application)) {
                Log.d("network", "boolean:  ${hasInternetConnection(requireActivity().application)}")
                if (email.isNotBlank() && password.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            firebaseAuth.signInWithEmailAndPassword(email, password).await()
                            checkSignInState()
                        } catch (e: FirebaseAuthException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to login, try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("authloginFailed", e.stackTrace.toString())
                            }
                        }
                    }
                }
            } else { Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show() }
        } catch (e: FirebaseNetworkException){ Log.e("networkException", e.message!!) }
    }

    private fun checkSignInState() {
        if (firebaseAuth.currentUser != null){
            val uid = firebaseAuth.currentUser?.uid.toString()
            firestore.collection("users")
                .whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (data in task.result!!) {
                            userdata.afterLoginUserData = data.toObject(UserModel::class.java)
                        }
                        findNavController().navigate(R.id.action_signInFragment_to_mainActivity)
                        activity?.finish()
                    }
                }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}