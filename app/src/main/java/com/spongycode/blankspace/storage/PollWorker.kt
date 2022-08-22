package com.spongycode.blankspace.storage

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firebaseAuth
import com.spongycode.blankspace.ui.main.QueryPreferenc
import com.spongycode.blankspace.util.Constants
import com.spongycode.blankspace.util.Constants.ACTION_SHOW_NOTIFICATION
import com.spongycode.blankspace.util.Constants.NOTIFICATION
import com.spongycode.blankspace.util.Constants.PERM_PRIVATE
import com.spongycode.blankspace.util.Constants.REQUEST_CODE

private const val CHANNEL_ID = "1"

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun doWork(): Result {
        // group chat
        val chatMessages = mutableListOf<ChatMessage>()
        val query = QueryPreferenc.getLastResultId(context)
        var string: String
        var name: String

        // private chcta
        val messageList = mutableListOf<ChatMessage>()
        val queryText = QueryPreferenc.getLastResultIdText(context)
        var stringText = ""
        var nameText = ""

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        // group caht listener
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
                    name = chatMessages[0].nameSender
                    Log.d("messageJn5", "mes: $string, $query")
                    if (query != string) {
                        // it intent for an Activity in your app
                        QueryPreferenc.setLastResultId(context, string)

                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("group message from $name")
                            .setContentText(string)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                        showBackgroundNotification(0, notification)
                    }
                }
            }

        val sender = firebaseAuth.currentUser!!.uid
        Firebase.firestore.collection("latest/messages/$sender")
            .orderBy("messageTime").addSnapshotListener { querySnapshot, error ->

                error?.let {
                    Log.w("Lmessages", error.message!!)
                    return@addSnapshotListener
                }

                messageList.clear()
                querySnapshot?.let {

                    // clear the list and message through every message
                    // thinking about this, it's better to user on type.added
                    for (dc in it.documentChanges) {
                        val chat = dc.document.toObject<ChatMessage>()
                        messageList.add(0, chat)
                    }
                    if (messageList.isNotEmpty()) {
                        stringText = messageList[0].messageText
                        nameText = messageList[0].nameSender
                    }
                    if (stringText != queryText && stringText.isNotEmpty()) {
                        QueryPreferenc.setLastResultIdText(context, stringText)

                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("private text from $nameText,")
                            .setContentText(stringText)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                        showBackgroundNotification(0, notification)
                    }

                }
            }

        Log.d("query", "mes: $stringText / $queryText")

        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }

        context.sendOrderedBroadcast(intent, PERM_PRIVATE)

    }

}