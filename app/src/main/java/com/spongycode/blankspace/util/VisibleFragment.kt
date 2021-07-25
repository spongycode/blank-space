package com.spongycode.blankspace.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.Fragment
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.Constants.ACTION_SHOW_NOTIFICATION
import com.spongycode.blankspace.util.Constants.PERM_PRIVATE

abstract class VisibleFragment: Fragment() {

    private val onShowNotification = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            Log.d("receiver", "cancel notification")
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_SHOW_NOTIFICATION)
        (activity as  MainActivity).registerReceiver(
            onShowNotification,
            filter,
            PERM_PRIVATE,
            null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}