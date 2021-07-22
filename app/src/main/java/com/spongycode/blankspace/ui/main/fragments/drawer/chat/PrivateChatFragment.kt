package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.*
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.Constants
import com.spongycode.blankspace.util.Constants.ImageShare
import com.spongycode.blankspace.viewmodel.ChatViewModel
import com.squareup.picasso.Picasso
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

//        chatViewModel.receiveChatMessages("user-messages/${sender.userId}/${receiver.userId}").observe(
//            viewLifecycleOwner, {
//                chatViewModel.chatMessages.addAll(it)
//                binding.list.adapter = PrivateChatAdapter(chatViewModel.chatMessages)
//                binding.list.addItemDecoration(
//                    DividerItemDecoration(
//                        requireContext(),
//                        DividerItemDecoration.VERTICAL
//                    )
//                )
//                binding.list.adapter?.notifyDataSetChanged()
//                // scroll to the just received message
//                binding.list.scrollToPosition(chatViewModel.chatMessages.size - 1)
//            }
//        )

        binding.apply {

            messageText.setText(chatViewModel.currentText)
            // only send the message if there's text entered
            // clear the textField after sending the message
            messageSend.setOnClickListener {
                if (messageText.text!!.isNotBlank()) {
                    sendMessage(receiver, sender, messageText.text.toString()); messageText.text?.clear()
                } else return@setOnClickListener
            }
            imageSend.setOnClickListener {
                val gallery = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                )
                startActivityForResult(gallery, 5)
            }

        }

        // change app bar title to receivers name
        (activity as MainActivity).supportActionBar?.title = receiver.username
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 5 && data != null && data.data != null) {
            loadImage(data.data!!)
        }
    }

    private fun loadImage(mUri : Uri){

        val bundle = bundleOf(ImageShare to mUri.toString(), "s" to sender, "r" to receiver)
        findNavController().navigate(R.id.imageShareFragment, bundle)
    }

    fun sendMessage(r: UserModel, s: UserModel, messageText: String, messageImage: String = ""){

        // reference of the chatRoom for the sender and receiver of the message
        val senderReference = Firebase.firestore
            .collection("user-messages/${s.userId}/${r.userId}")
        val receiverReference = Firebase.firestore
            .collection("user-messages/${r.userId}/${s.userId}")

        s?.let { sender ->
            if (r == null) return

            // create the message typed by the user
            val message = ChatMessage(
                UUID.randomUUID().toString(),
                messageText,
                Calendar.getInstance().timeInMillis,
                r.userId,
                r.username,
                "",
                s.userId,
                s.username,
                "",
                messageImage
            )

            senderReference.add(message)
            receiverReference.add(message)

            // send the same message to the latest message node
            // every message sent overrides the previous
            val messageMap = hashMapOf(
                "messageId" to message.messageId,
                "messageReceiverId" to message.messageReceiverId,
                "nameReceiver" to r.username,
                "profilePictureReceiver" to r.imageUrl,
                "messageSenderId" to s.userId,
                "nameSender" to s.username,
                "profilePictureSender" to s.imageUrl,
                "messageText" to messageText,
                "messageTime" to message.messageTime,
                "messageImage" to messageImage
            )

            Firebase.firestore
                .collection("latest/messages/${s.userId}")
                .document(r.userId)
                .set(messageMap, SetOptions.merge())

            Firebase.firestore
                .collection("latest/messages/${r.userId}")
                .document(s.userId)
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

                        chatMessages.sortByDescending { it.messageTime }
                        binding.list.adapter = PrivateChatAdapter(chatMessages)
                        binding.list.addItemDecoration(
                            DividerItemDecoration(
                                requireContext(),
                                DividerItemDecoration.VERTICAL
                            )
                        )
                        binding.list.adapter?.notifyDataSetChanged()
                        // scroll to the just received message
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
            internal val rImage: ShapeableImageView = binding.rImage
        }

        inner class PrivateChatReceiverViewHolder(binding: LeftsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val lMessage: MaterialTextView = binding.lMessage
            internal val lImage: ShapeableImageView = binding.lImage
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
                    if (message.image.isNotBlank()){
                        this.rImage.visibility = View.VISIBLE
                        Picasso.get().load(message.image.toUri()).into(this.rImage)
                    } else {
                        this.rImage.visibility = View.GONE
                    }
                }
                if (this is PrivateChatReceiverViewHolder) {
                    this.lMessage.text = message.messageText
                    if (message.image.isNotBlank()){
                        this.lImage.visibility = View.VISIBLE
                        Picasso.get().load(message.image.toUri()).into(this.lImage)
                    } else {
                        this.lImage.visibility = View.GONE
                    }
                }
            }
        }
        override fun getItemCount() = messages.size
    }
}