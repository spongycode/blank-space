package com.spongycode.blankspace.ui.main.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.spongycode.blankspace.ui.main.fragments.drawer.chat.ChatScreenFragment
import com.spongycode.blankspace.ui.main.fragments.drawer.chat.GroupChatFragment

class ChatAdapter (
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int

):
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                GroupChatFragment()
            }
            1 -> {
                ChatScreenFragment()
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }

}