package com.spongycode.blankspace.ui.main.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentTabLayoutBinding
import com.spongycode.blankspace.ui.main.adapters.MainAdapter

class TabLayoutFragment : Fragment() {

    private var _binding: FragmentTabLayoutBinding? = null
    private val binding get() = _binding!!
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTabLayoutBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar?.setIcon(R.drawable.ic_troll_face)
        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)!!.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_burger)


        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            (activity as AppCompatActivity?)!!.findViewById(R.id.drawerLayout),
            (activity as AppCompatActivity?)!!.findViewById(R.id.toolbar),
            R.string.open_drawer,
            R.string.close_drawer
        )
        (activity as AppCompatActivity?)!!.findViewById<DrawerLayout>(R.id.drawerLayout)
            .addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
        binding.toolbar.setNavigationOnClickListener {
            (activity as AppCompatActivity?)!!.findViewById<DrawerLayout>(R.id.drawerLayout).openDrawer(
                GravityCompat.START
            )
        }


        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        tabLayout.addTab(tabLayout.newTab().setText("Home"))
        tabLayout.addTab(tabLayout.newTab().setText("Generate"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val tabAdapter = MainAdapter(
            this@TabLayoutFragment.requireContext(), childFragmentManager, tabLayout.tabCount
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
        return binding.root
    }
}