package com.spongycode.blankspace.ui.main.fragments.drawer.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spongycode.blankspace.databinding.FragmentFMemesBinding

class FMemesFragment: Fragment() {

    private var _binding: FragmentFMemesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFMemesBinding.inflate(inflater, container, false)
        return binding.root
    }

}