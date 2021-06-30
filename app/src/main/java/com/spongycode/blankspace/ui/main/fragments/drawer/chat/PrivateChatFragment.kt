package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentPrivateChatBinding
import com.spongycode.blankspace.databinding.LeftsidemessageBinding
import com.spongycode.blankspace.databinding.RightsidemessageBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.Constants
import com.spongycode.blankspace.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PrivateChatFragment: Fragment() {

    private var _binding: FragmentPrivateChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var lMessageBinding: LeftsidemessageBinding
    private lateinit var rMessageBinding: RightsidemessageBinding
    private val chatViewModel: ChatViewModel = MainActivity.chatViewModel
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var receiver: UserModel
    private lateinit var sender: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivateChatBinding.inflate(inflater, container, false)

        sender = arguments?.getSerializable(Constants.USERR_KEY) as UserModel
        Log.d("sender", "sender: $sender")
        // get the user selected by the sender.
        receiver = arguments?.getSerializable(Constants.USER_KEY) as UserModel
        Log.d("sender", "receiver: $receiver")

        receiveMessage()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            messageText.setText(chatViewModel.currentText)
            // only send the message if there's text entered
            // clear the textField after sending the message
            messageSend.setOnClickListener {
                if (messageText.text!!.isNotBlank()) {
                    sendMessage(); messageText.text?.clear()
                } else return@setOnClickListener
            }
        }

        // change app bar title to receivers name
        (activity as MainActivity).supportActionBar?.title = receiver.username
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.private_chat, menu)
    }

    // Maybe we'll have time to add voice or video chat
    // But first we need to be able to share images in chat
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.voiceCall -> {
            Toast.makeText(requireContext(),
                "you tried to call ${receiver.username}",
                Toast.LENGTH_SHORT).show()
            true
        }

        R.id.videoCall -> {
            Toast.makeText(requireContext(),
                "you tried to video ${receiver.username}",
                Toast.LENGTH_SHORT).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun sendMessage(){

        // reference of the chatRoom for the sender and receiver of the message
        val senderReference = Firebase.firestore
            .collection("user-messages/${sender.userId}/${receiver.userId}")
        val receiverReference = Firebase.firestore
            .collection("user-messages/${receiver.userId}/${sender.userId}")

        sender?.let { sender ->
            if (receiver == null) return

            // create the message typed by the user
            val message = ChatMessage(
                UUID.randomUUID().toString(),
                binding.messageText.text.toString(),
                sender.userId,
                receiver.userId,
                Calendar.getInstance().timeInMillis / 1000
            )

            senderReference.add(message)
            receiverReference.add(message)

            // send the same message to the latest message node
            // every message sent overrides the previous
            val messageMap = hashMapOf(
                "messageId" to message.messageId,
                "messageReceiverId" to message.messageReceiverID,
                "nameReceiver" to receiver.username,
                "profilePictureReceiver" to receiver.imageUrl,
                "messageSenderId" to sender.userId,
                "nameSender" to sender.username,
                "profilePictureSender" to sender.imageUrl,
                "messageText" to message.messageText,
                "messageTime" to message.messageTime
            )

            Firebase.firestore
                .collection("latest/messages/${sender.userId}")
                .document(receiver.userId)
                .set(messageMap, SetOptions.merge())

            Firebase.firestore
                .collection("latest/messages/${receiver.userId}")
                .document(sender.userId)
                .set(messageMap, SetOptions.merge())
        }

    }

    // this will go to repo
    private fun receiveMessage(){

        CoroutineScope(Dispatchers.IO).launch{  // listen to every event at this collection
            // add every new messasge to the messages list
            Firebase.firestore.collection(
                "user-messages/${sender.userId}/${receiver.userId}"
            )
                .orderBy("messageTime")
                .addSnapshotListener { querySnapshot, error ->

                    // if error then log it
                    error?.let {
                        Log.d("receiveMessage", error.message!!)
                    }

                    // clear the list for older messages and redownload all messages again
                    chatMessages.clear()
                    Log.d("query", "quer: ${querySnapshot?.documents}, ${sender.userId}/${receiver.userId}")
                    querySnapshot?.let {
                        for (document in it) {
                            val message = document.toObject<ChatMessage>()
                            chatMessages.add(message)
                        }

                        // scroll to the just received message
                        binding.list.adapter = PrivateChatAdapter(chatMessages)
                        binding.list.addItemDecoration(
                            DividerItemDecoration(
                                requireContext(),
                                DividerItemDecoration.VERTICAL
                            )
                        )
                        binding.list.adapter?.notifyDataSetChanged()
                        binding.list.scrollToPosition(chatMessages.size - 1)
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        chatViewModel.currentText = binding.messageText.text.toString()
    }

    // basic adapter
    inner class PrivateChatAdapter(private val messages: List<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        inner class PrivateChatSenderViewHolder(binding: RightsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
                internal val rMessage: MaterialTextView = binding.rMessage
            }

        inner class PrivateChatReceiverViewHolder(binding: LeftsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
                internal val lMessage: MaterialTextView = binding.lMessage
            }

        override fun getItemViewType(position: Int): Int {
            return if(messages[position].messageSenderID
                == FirebaseAuth.getInstance().currentUser!!.uid) 0 else 1
        }

        // ???
//        private val privateChatDiffUtil = object: DiffUtil.ItemCallback<ChatMessage>(){
//            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
//                return oldItem.messageId == newItem.messageId
//            }
//
//            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
//                return oldItem.messageText == newItem.messageText
//            }
//        }
//
//        val privateChatDiffer = AsyncListDiffer(this, privateChatDiffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0){
                rMessageBinding = RightsidemessageBinding.inflate(LayoutInflater.from(parent.context))
                PrivateChatSenderViewHolder(rMessageBinding)
            } else {
                lMessageBinding = LeftsidemessageBinding.inflate(LayoutInflater.from(parent.context))
                PrivateChatReceiverViewHolder(lMessageBinding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages.get(position)
            with(holder){
                if (this is PrivateChatSenderViewHolder) {
                    this.rMessage.text = message.messageText
                }
                if (this is PrivateChatReceiverViewHolder) {
                    this.lMessage.text = message.messageText
                }
            }
        }
        override fun getItemCount() = messages.size
    }
}