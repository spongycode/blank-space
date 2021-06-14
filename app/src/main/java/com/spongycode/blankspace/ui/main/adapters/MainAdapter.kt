package com.spongycode.blankspace.ui.main.adapters

import com.spongycode.blankspace.ui.main.fragments.GenerateFragment
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.spongycode.blankspace.ui.main.fragments.MainFragment

@Suppress("DEPRECATION")
internal class MainAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int

) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MainFragment()
            }
            1 -> {
                GenerateFragment()
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }

}