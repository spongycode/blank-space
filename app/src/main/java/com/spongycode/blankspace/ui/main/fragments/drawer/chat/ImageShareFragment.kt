package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.spongycode.blankspace.databinding.FragmentImageShareBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.storage.sendGroupMessage
import com.spongycode.blankspace.storage.sendMessage
import com.spongycode.blankspace.util.Constants.ImageShare
import com.spongycode.blankspace.util.Constants.groupId
import com.squareup.picasso.Picasso

class ImageShareFragment: Fragment() {

    private var _binding: FragmentImageShareBinding? = null
    private val binding get() = _binding!!
    private var myImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageShareBinding.inflate(inflater, container, false)

        myImage = arguments?.get(ImageShare) as String
        val r = arguments?.get("r") as UserModel?
        val s = arguments?.get("s") as UserModel
        lateinit var downloadUri : Uri


        binding.apply {
            Picasso.get().load(myImage!!.toUri()).into(image)
            imageSend.setOnClickListener {

                val ref = Firebase.storage.reference.child("chatImages/${System.currentTimeMillis()}")
                val uploadTask = ref.putFile(myImage!!.toUri())

                val urlTask = uploadTask.continueWithTask{ task ->
                    if (!task.isSuccessful){ task.exception?.let { throw it }}

                    ref.downloadUrl
                }.addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val downloadUri: Uri? = task.result
                        if (r != null){
                            sendMessage(r, s, message.text.toString(), downloadUri.toString())
                            findNavController().navigateUp()
                        }else {
                            sendGroupMessage(groupId, s, message.text.toString(), downloadUri.toString())
                            findNavController().navigateUp()
                        }
                    } else {
                        Log.e("taskUpload", "upload error")
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}