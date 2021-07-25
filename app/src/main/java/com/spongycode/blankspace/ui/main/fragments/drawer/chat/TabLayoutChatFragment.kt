package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentTabLayoutBinding
import com.spongycode.blankspace.ui.main.adapters.ChatAdapter
import com.spongycode.blankspace.util.VisibleFragment

class TabLayoutChatFragment: VisibleFragment() {

    private var _binding: FragmentTabLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTabLayoutBinding.inflate(inflater, container, false)

        binding.toolbar.visibility = View.GONE

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        tabLayout.addTab(tabLayout.newTab().setText("Group Chat"))
        tabLayout.addTab(tabLayout.newTab().setText("Chat List"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val tabAdapter = ChatAdapter(
            this@TabLayoutChatFragment.requireContext(), childFragmentManager, tabLayout.tabCount
        )
        viewPager.adapter = tabAdapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigate(R.id.action_tabLayoutChatFragment_to_tabLayoutFragment)
        }
        return binding.root
    }

}