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
import com.spongycode.blankspace.databinding.FragmentSignupBinding
import com.spongycode.blankspace.model.modelLoginUser.LoginUser
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.auth.AuthActivity.Companion.usersCollectionReference
import com.spongycode.blankspace.ui.auth.fragments.SignInFragment.Companion.firestore
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

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

            signUptoSignInTV.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }

        }
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String
    ) {
        if (email.isNotBlank() && password.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                if (Helper.isUniqueUsername(name)) {
                    try {
                        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                        val userId = firebaseAuth.currentUser!!.uid
                        usersCollectionReference.document(userId)
                            .set(
                                LoginUser(
                                    email = email,
                                    imageUrl = "",
                                    username = name,
                                    userId = userId
                                )
                            )
                        checkSignUpState()
                    } catch (e: FirebaseAuthException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to register, try again",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.w("authregisterFailed", e.stackTrace.toString())
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Pick unique username",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun checkSignUpState() {
        if (firebaseAuth.currentUser != null) {
            val uid = firebaseAuth.currentUser?.uid.toString()
            firestore.collection("users")
                .whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (data in task.result!!) {
                            userdata.afterLoginUserData = data.toObject(LoginUser::class.java)
                        }
                    }
                }

            findNavController().navigate(R.id.action_signUpFragment_to_mainActivity)
            activity?.finish()
        }


    }

    override fun onStart() {
        super.onStart()
        checkSignUpState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}