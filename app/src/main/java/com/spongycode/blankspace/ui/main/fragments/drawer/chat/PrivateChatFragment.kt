package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.model.modelChat.User
import com.spongycode.blankspace.databinding.FragmentPrivateChatBinding
import com.spongycode.blankspace.databinding.LeftsidemessageBinding
import com.spongycode.blankspace.databinding.RightsidemessageBinding
import com.spongycode.blankspace.ui.main.MainActivity
import java.util.*

class PrivateChatFragment: Fragment() {

    private var _binding: FragmentPrivateChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var rItemBinding: RightsidemessageBinding
    private lateinit var lItemBinding: LeftsidemessageBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private val receiver = arguments?.getSerializable(RECEIVER) as User
    private val sender = arguments?.getSerializable(USER) as User

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
        // Set view adapter
        binding.list.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL))
            adapter = PrivateChatAdapter()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = receiver.name
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        receiveMessage()
        binding.apply {
            messageSend.setOnClickListener {
                if (messageText.text!!.isNotEmpty()){
                    sendMessage(); messageText.text?.clear()
                } else return@setOnClickListener
            }
        }
    }

    private fun sendMessage(){

        // reference of the chatRoom for the sender and receiver of the message
        val senderReference = Firebase.firestore
            .collection("/user-messages/${sender.userId}/${receiver.userId}")
        val receiverReference = Firebase.firestore
            .collection("/user-messages/${receiver.userId}/${sender.userId}")

        sender?.let { sender ->
            if (receiver == null) return@let

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
                "nameReceiver" to receiver.name,
                "profilePictureReceiver" to receiver.profilePicture,
                "messageSenderId" to sender.userId,
                "nameSender" to sender.name,
                "profilePictureSender" to sender.profilePicture,
                "messageText" to message.messageText,
                "messageTime" to message.messageTime
            )

            Firebase.firestore
                .collection("/latest/messages/${sender.userId}")
                .document(receiver.userId)
                .set(messageMap, SetOptions.merge())

            Firebase.firestore
                .collection("/latest/messages/${receiver.userId}")
                .document(sender.userId)
                .set(messageMap, SetOptions.merge())
        }

    }

    private fun receiveMessage(){

        // listen to every event at this collection
        // add every new messasge to the messages list
        Firebase.firestore.collection(
            "/user-messages/${sender.userId}/${receiver.userId}")
            .orderBy("messageTime")
            .addSnapshotListener { querySnapshot, error ->

                // if error then log it
                error?.let {
                    Log.d("receiveMessage", error.message!!)
                }

                // clear the list for older messages and redownload all messages again
                chatMessages.clear()
                querySnapshot?.let{
                    for (document in it){
                        val message = document.toObject<ChatMessage>()
                        chatMessages.add(message)
                    }

                    // clear duplicates from the list and submit the list
                    chatMessages.toSet()
                    PrivateChatAdapter().privateChatDiffer.submitList(chatMessages.toList())
                    // scroll to the just received message
                    binding.list.scrollToPosition(chatMessages.size - 1)
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private inner class PrivateChatAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        inner class PrivateChatLeftViewHolder(binding: LeftsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val lMessage = lItemBinding.lMessage
        }
        inner class PrivateChatRightViewHolder(binding: RightsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val rMessage = rItemBinding.rMessage
        }

        private val privateChatDiffUtil = object: DiffUtil.ItemCallback<ChatMessage>(){
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem.messageTime == newItem.messageTime
            }

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem.messageText == newItem.messageText
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (privateChatDiffer.currentList[position].messageSenderID
                == currentUser!!.uid) 0 else 1
        }

        val privateChatDiffer = AsyncListDiffer(this, privateChatDiffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                rItemBinding = RightsidemessageBinding.inflate(LayoutInflater.from(parent.context))
                PrivateChatRightViewHolder(rItemBinding)
            } else{
                lItemBinding = LeftsidemessageBinding.inflate(LayoutInflater.from(parent.context))
                PrivateChatLeftViewHolder(lItemBinding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(privateChatDiffer.currentList[position]){
                if (holder is PrivateChatRightViewHolder){
                    holder.rMessage.text = messageText
                } else {
                    (holder as PrivateChatLeftViewHolder).lMessage.text = messageText
                }
            }
        }

        override fun getItemCount() = privateChatDiffer.currentList.size
    }
}