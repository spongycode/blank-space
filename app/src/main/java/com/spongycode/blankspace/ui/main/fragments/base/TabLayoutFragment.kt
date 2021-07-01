package com.spongycode.blankspace.ui.main.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentTabLayoutBinding
import com.spongycode.blankspace.ui.main.adapters.MainAdapter
import com.spongycode.blankspace.util.userdata

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
        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        (activity as AppCompatActivity?)!!.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24)


        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            (activity as AppCompatActivity?)!!.findViewById(R.id.drawerLayout),
            (activity as AppCompatActivity?)!!.findViewById(R.id.toolbar),
            R.string.open_drawer,
            R.string.close_drawer
        )
        val header = (activity as AppCompatActivity?)!!
            .findViewById<NavigationView>(R.id.navigationView)
            .getHeaderView(0)
        header.findViewById<TextView>(R.id.nav_header_username).text = userdata.afterLoginUserData.username
        Glide.with(this).load(userdata.afterLoginUserData.imageUrl)
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .into(header.findViewById(R.id.nav_header_iv))

        (activity as AppCompatActivity?)!!.findViewById<DrawerLayout>(R.id.drawerLayout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        (activity as AppCompatActivity?)!!.findViewById<DrawerLayout>(R.id.drawerLayout)
            .addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
        binding.toolbar.setNavigationOnClickListener {
            (activity as AppCompatActivity?)!!.findViewById<DrawerLayout>(R.id.drawerLayout).openDrawer(
                GravityCompat.START
            )
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_dehaze_24)

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
        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().finish()
        }
        return binding.root
    }
}