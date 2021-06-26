package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spongycode.blankspace.databinding.FragmentPrivateChatBinding

class GroupChatFragment: Fragment() {

    private var _binding: FragmentPrivateChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivateChatBinding.inflate(inflater, container, false)
        return binding.root
    }

}