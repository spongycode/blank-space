package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.accounts.NetworkErrorException
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.databinding.FragmentGroupChatBinding
import com.spongycode.blankspace.databinding.LeftsidemessageBinding
import com.spongycode.blankspace.databinding.RightsidemessageBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.Constants.groupId
import com.spongycode.blankspace.util.NetworkCheck
import com.spongycode.blankspace.viewmodel.ChatViewModel
import java.util.*


class GroupChatFragment: Fragment() {

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var lMessageBinding: LeftsidemessageBinding
    private lateinit var rMessageBinding: RightsidemessageBinding
    private val chatViewModel: ChatViewModel = MainActivity.chatViewModel
    private val chatMessages = mutableListOf<ChatMessage>()
    private var sender = UserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        chatViewModel.user.observe(viewLifecycleOwner, {
            sender = it.get(0)
        })

        try{
            if (NetworkCheck.hasInternetConnection((activity as MainActivity).application)){
                chatViewModel.receiveMessage("user-messages/group/$groupId").observe(viewLifecycleOwner, {
                    chatViewModel.groupMessages.addAll(it)
                })
                Log.d("error", "group: ${chatViewModel.groupMessages}")
                binding.list.apply {
                    adapter = GroupChatAdapter(chatViewModel.groupMessages)
                    addItemDecoration(DividerItemDecoration((activity as MainActivity).baseContext,
                        DividerItemDecoration.VERTICAL))
                    adapter?.notifyDataSetChanged()
                    scrollToPosition(chatMessages.size - 1)
                }
            }else {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            }
        } catch (e: NetworkErrorException) { Log.e("networkException", e.message!!) }

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

        return binding.root
    }

    // I could move this to chatToFirebase.kt, but then it would take a lot of parameters.
    private fun sendMessage(){

        // reference of the chatRoom for the sender and receiver of the message
        val senderReference = Firebase.firestore
            .collection("user-messages/group/$groupId")

        sender?.let { sender ->

            // create the message typed by the user
            val message = ChatMessage(
                UUID.randomUUID().toString(),
                binding.messageText.text.toString(),
                Calendar.getInstance().timeInMillis,
                groupId,
                "",
                "",
                sender.userId,
                sender.username,
                ""
            )

            senderReference.add(message)
        }

    }


    override fun onPause() {
        super.onPause()
        chatViewModel.currentText = binding.messageText.text.toString()
    }

    // basic adapter
    private inner class GroupChatAdapter(private val messages: List<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        inner class PrivateChatSenderViewHolder(binding: RightsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val rMessage: MaterialTextView = binding.rMessage
        }

        inner class PrivateChatReceiverViewHolder(binding: LeftsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val lMessage: MaterialTextView = binding.lMessage
            internal val lName: MaterialTextView = binding.lName
        }

        override fun getItemViewType(position: Int): Int {
            return if(messages[position].messageSenderId
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
//                    well, i guess i will have to build an unique message with all features, better than having three
                    this.lMessage.text = message.messageText
                    this.lName.text = message.nameSender
                }
            }
        }
        override fun getItemCount() = messages.size
    }
}