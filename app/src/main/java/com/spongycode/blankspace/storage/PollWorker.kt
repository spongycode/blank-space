package com.spongycode.blankspace.storage

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.QueryPreferenc
import com.spongycode.blankspace.util.Constants

private const val CHANNEL_ID = "1"

class PollWorker(val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        val chatMessages = mutableListOf<ChatMessage>()
        val query = QueryPreferenc.getLastResultId(context)
        var string = ""

        val a = Firebase.firestore
            .collection("user-messages/group/${Constants.groupId}")
        a
            .orderBy("messageTime")
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.w("error", error)
                }

                value?.let {
                    chatMessages.clear()
                    for (doc in it) {
                        val message = doc.toObject<ChatMessage>()
                        chatMessages.add(message)
                    }
                    chatMessages.sortByDescending { it.messageTime }
                    string = chatMessages[0].messageText
                    Log.d("messageJn5", "mes: $string, $query")
                    if (query == string) {
                        // honestly, just do nothing
                    } else { // Create an explic
                        // it intent for an Activity in your app
                        QueryPreferenc.setLastResultId(context, string)

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent: PendingIntent =
                            PendingIntent.getActivity(context, 0, intent, 0)

                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("stuff")
                            .setContentText(string)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)

                        with(NotificationManagerCompat.from(context)) {
                            notify(0, notification.build())
                        }
                    }
                }
            }
        return Result.success()
    }

    fun getNotification(title: String, text: String){
        // Create an explicit intent for an Activity in your app
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 3, intent, 0)
//
//        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.logo)
//            .setContentTitle(title)
//            .setContentText(text)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)

    }
}