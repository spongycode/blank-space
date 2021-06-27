package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.databinding.FragmentPrivateChatBinding
import com.spongycode.blankspace.databinding.LeftsidemessageBinding
import com.spongycode.blankspace.databinding.RightsidemessageBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.viewmodel.ChatViewModel
import java.util.*

class GroupChatFragment: Fragment() {

    /*
    * the layout is as basic, but still i think we should separate it/ create it's own layout,
    * there are tiny diferences and later it will have way more diferences
    *
    * how will it work?
    * in private chat the message is send to path - sender/receiver and vice-versa
    * in group chat we still want to just send messages, but slightly differently
    * sender/group,  group/sender, no receiver, i think so, we'll just differ from senders
    *
    * there's just no receiver Id, but it can't be sender/group, because that will be a different path
    * for every user, and the path needs to be the same
    * message-group, message-group, no sender
    * and then just download all messages from the group, but the message should still hold
    * info of the sender and the receiver(groupId)
    * */

    private var _binding: FragmentPrivateChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var lMessageBinding: LeftsidemessageBinding
    private lateinit var rMessageBinding: RightsidemessageBinding
    private val chatViewModel: ChatViewModel = MainActivity.chatViewModel
    private val chatMessages = mutableListOf<ChatMessage>()
    private var sender = UserModel("asfkljalsdkjf", "", "antonio")
    private val groupId = "BlankSpaceGroup"

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

        chatViewModel.user.observe(viewLifecycleOwner, {
            sender = it.get(0)
        })
        Log.d("sender", "sender: $sender")

        receiveMessage()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveMessage()

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
        (activity as MainActivity).supportActionBar?.title = "Blank Space"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun sendMessage(){

        // reference of the chatRoom for the sender and receiver of the message
        val senderReference = Firebase.firestore
            .collection("user-messages/group-chat/$groupId")

        sender?.let { sender ->

            // create the message typed by the user
            val message = ChatMessage(
                UUID.randomUUID().toString(),
                binding.messageText.text.toString(),
                sender.userId,
                groupId,
                Calendar.getInstance().timeInMillis / 1000
            )

            senderReference.add(message)

            // send the same message to the latest message node
            // every message sent overrides the previous
            // will think about this tomorrow, but no receiver,
            // i just need the receiver logic to set up the view, in this case it's always the group
            // only thing that will change is the message, but in chat we'll need to know who sent it
            // and we have that info from the sender, we don't hve that...
            // we do have it in the map below, but that's for chatScreen, i need it now in the chatMessage Model
            // only names, no pictures needed

            // on the other hand, messages in this fragment won't be presented on chatScreen
            // maybe later when we add features, and users find themselves able to create groups, but
            // group chat fragment is exactly what the name says. (general chat)
//            val messageMap = hashMapOf(
//                "messageId" to message.messageId,
//                "messageReceiverId" to message.messageReceiverID,
//                "nameReceiver" to receiver.username,
//                "profilePictureReceiver" to receiver.imageUrl,
//                "messageSenderId" to sender.userId,
//                "nameSender" to sender.username,
//                "profilePictureSender" to sender.imageUrl,
//                "messageText" to message.messageText,
//                "messageTime" to message.messageTime
//            )
//
//            Firebase.firestore
//                .collection("latest/messages/${sender.userId}")
//                .document(receiver.userId)
//                .set(messageMap, SetOptions.merge())
//
//            Firebase.firestore
//                .collection("latest/messages/${receiver.userId}")
//                .document(sender.userId)
//                .set(messageMap, SetOptions.merge())
        }

    }

    // this will go to repo
    private fun receiveMessage(){

        // listen to every event at this collection
        // add every new messasge to the messages list
        Firebase.firestore.collection("user-messages/group-chat/$groupId")
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
                        // dont know if i should create a GroupMessage data class, or modify chatMessage
                        val message = document.toObject<ChatMessage>()
                        Log.d("message", "messaeg: $message")
                        chatMessages.add(message)
                    }

                    // scroll to the just received message
                    binding.list.adapter = PrivateChatAdapter(chatMessages)
                    binding.list.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    binding.list.adapter?.notifyDataSetChanged()
                    binding.list.scrollToPosition(chatMessages.size - 1)
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