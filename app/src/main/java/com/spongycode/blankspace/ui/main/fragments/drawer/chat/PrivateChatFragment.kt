package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.databinding.*
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.storage.sendMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.QueryPreferenc
import com.spongycode.blankspace.ui.main.fragments.base.PhotoViewerDialog
import com.spongycode.blankspace.util.Constants
import com.spongycode.blankspace.util.gallery
import com.spongycode.blankspace.util.loadImage
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
    private val chatMessages = mutableListOf<ChatMessage>(ChatMessage("randomId", "hi, you have no messages"))
    private lateinit var receiver: UserModel
    private lateinit var sender: UserModel
    var textQuery = ""

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
        // get the user selected by the sender.
        receiver = arguments?.getSerializable(Constants.USER_KEY) as UserModel

        receiveMessage()

        binding.list.addItemDecoration(
            DividerItemDecoration(
                (activity as MainActivity).baseContext,
                DividerItemDecoration.VERTICAL
            )
        )

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
            loadImage(data.data!!, this, sender, receiver)
        }
    }

    // this will go to repo
    fun receiveMessage(): ChatMessage{

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
                    querySnapshot?.let {
                        for (document in it) {
                            val message = document.toObject<ChatMessage>()
                            chatMessages.add(message)
                        }

                        chatMessages.sortByDescending { it.messageTime }
                        binding.list.adapter = PrivateChatAdapter(chatMessages)
                        binding.list.adapter?.notifyDataSetChanged()
                        // scroll to the just received message
                        binding.list.scrollToPosition(0)
                        textQuery = chatMessages[0].messageText
                    }
                }
        }
        return chatMessages[0]
    }

    override fun onPause() {
        super.onPause()
        QueryPreferenc.setLastResultIdText((activity as MainActivity).applicationContext, textQuery)
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
                    if (message.image.isNotBlank()){
                        this.rImage.visibility = View.VISIBLE
                        Picasso.get().load(message.image.toUri()).into(this.rImage)
                        this.rImage.setOnClickListener {
                            PhotoViewerDialog.newInstance(message.image).show(parentFragmentManager, "hello")
                        }
                        if (this.rMessage.text.isBlank()) this.rMessage.visibility = View.GONE
                        else this.rImage.visibility = View.VISIBLE
                    } else {
                        this.rImage.visibility = View.GONE
                    }
                }
                if (this is PrivateChatReceiverViewHolder) {
                    this.lMessage.text = message.messageText
                    this.lName.visibility = View.GONE
                    if (message.image.isNotBlank()){
                        this.lImage.visibility = View.VISIBLE
                        Picasso.get().load(message.image.toUri()).into(this.lImage)
                        this.lImage.setOnClickListener {
                            PhotoViewerDialog.newInstance(message.image).show(parentFragmentManager, "hello")
                        }
                        if (this.lMessage.text.isBlank()) this.lMessage.visibility = View.GONE
                        else this.lImage.visibility = View.VISIBLE
                    } else {
                        this.lImage.visibility = View.GONE
                    }
                }
            }
        }
        override fun getItemCount() = messages.size
    }
}