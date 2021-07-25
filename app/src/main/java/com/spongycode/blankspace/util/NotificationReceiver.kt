package com.spongycode.blankspace.util

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.spongycode.blankspace.util.Constants.REQUEST_CODE

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("receiver", "received: ${resultCode}")
        if (resultCode != Activity.RESULT_OK){
            // a foreground activity canceled the broadcast
            return
        }

        val requestCode = intent.getIntExtra(REQUEST_CODE, 0)
        val notification = intent.getParcelableExtra<Notification>(Constants.NOTIFICATION)!!

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(requestCode, notification)

    }

}