package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.spongycode.blankspace.databinding.FragmentImageShareBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.util.Constants.ImageShare
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
        val r = arguments?.get("r") as UserModel
        val s = arguments?.get("s") as UserModel


        binding.apply {
            Picasso.get().load(myImage!!.toUri()).into(image)
            imageSend.setOnClickListener {
                PrivateChatFragment().sendMessage(r, s, message.text.toString(), myImage!!)
                findNavController().navigateUp()
            }
        }

        return binding.root
    }


}