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
import com.spongycode.blankspace.databinding.FragmentSignupBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.auth.AuthActivity.Companion.usersCollectionReference
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.NetworkCheck.hasInternetConnection
import com.spongycode.blankspace.util.userdata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = AuthActivity().firebaseAuth
    private val firestore = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback {
            activity?.finish()
        }
        Helper.buttonEffect(binding.signUpButton, "#C665F37D")
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
        try{
            if (hasInternetConnection((activity as AuthActivity).application)){
                if (email.isNotBlank() && password.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (Helper.isUniqueUsername(name)) {
                            try {
                                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                                val userId = firebaseAuth.currentUser!!.uid
                                usersCollectionReference.document(userId)
                                    .set(
                                        UserModel(
                                            userId, email, name
                                        )
                                    )
                                checkSignUpState()
                            } catch (e: FirebaseAuthException) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.failed_to_register),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.change_user_name),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            } else { Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show() }
        } catch (e: FirebaseNetworkException){ Log.e(NETWORK_TAG, e.message!!) }
    }

    private fun checkSignUpState() {
        if (firebaseAuth.currentUser != null) {
            val uid = firebaseAuth.currentUser?.uid.toString()
            firestore.collection(USERS)
                .whereEqualTo(USER_ID, uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (data in task.result!!) {
                            userdata.afterLoginUserData = data.toObject(UserModel::class.java)
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

    companion object {
        private const val USERS = "users"
        private const val USER_ID = "userId"
        private const val NETWORK_TAG = "networkException"
    }
}