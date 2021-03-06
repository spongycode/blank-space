package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentChatScreenBinding
import com.spongycode.blankspace.databinding.ItemListofchatsBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.QueryPreferenc
import com.spongycode.blankspace.util.Constants.USERR_KEY
import com.spongycode.blankspace.util.Constants.USER_KEY
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatScreenFragment : Fragment() {

    private var _binding: FragmentChatScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatRoomBinding: ItemListofchatsBinding
    private val firebaseAuth = MainActivity.firebaseAuth
    private val chatViewModel = MainActivity.chatViewModel
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var currentUser: UserModel
    private var query = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatScreenBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigateUp()
        }

        chatViewModel.user.observe(viewLifecycleOwner) {
            currentUser = it.get(0)
            Log.d("user", "Itx : $currentUser")
        }

        listenForLatestMessages()
        binding.list.addItemDecoration(
            DividerItemDecoration(
                (activity as MainActivity).baseContext,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.fab.setOnClickListener { findNavController().navigate(R.id.listOfUsersFragment) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.title = "List of chats"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }

    // this needs to be in repository, same as send and receive message
    // and accessed from viewModel
    private fun listenForLatestMessages() {
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
                    if (messageList.isNotEmpty()) query = messageList[0].messageText
                    binding.list.apply {
                        adapter = ListOfChatsAdapter(messageList)
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
    }

    inner class ListOfChatsAdapter(private val listMessage: List<ChatMessage>) :
        RecyclerView.Adapter<ListOfChatsAdapter.ListOfChatsViewHolder>() {
        inner class ListOfChatsViewHolder(binding: ItemListofchatsBinding) :
            RecyclerView.ViewHolder(binding.root) {
            internal val profile: CircleImageView = binding.profile
            internal val name: MaterialTextView = binding.name
            internal val message: MaterialTextView = binding.message
            internal val messageTime: MaterialTextView = binding.time
        }

        // not working
        private val diffUtil = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(
                oldItem: ChatMessage,
                newItem: ChatMessage
            ): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(
                oldItem: ChatMessage,
                newItem: ChatMessage
            ): Boolean {
                return oldItem.messageTime == newItem.messageTime
            }
        }

        val listOfChatsDiffer = AsyncListDiffer(this, diffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfChatsViewHolder {
            chatRoomBinding = ItemListofchatsBinding.inflate(LayoutInflater.from(parent.context))
            return ListOfChatsViewHolder(chatRoomBinding)
        }

        override fun getItemCount() = listMessage.size

        override fun onBindViewHolder(holder: ListOfChatsViewHolder, position: Int) {
            val message = listMessage.get(position)
            val myDate = "dd/MM HH:mm"
            var user: UserModel
            with(holder) {
                with(message) {
                    holder.message.text = messageText.toString()
                    holder.messageTime.text = DateFormat.format(myDate, messageTime)

                    if (message.messageReceiverId == FirebaseAuth.getInstance().currentUser!!.uid) {
                        holder.name.text = message.nameSender
                        if (message.profilePictureSender.isNotBlank()) {
                            Picasso.get().load(message.profilePictureSender)
                                .into(holder.profile)
                        }
                    } else {
                        holder.name.text = message.nameReceiver
                        if (message.profilePictureReceiver.isNotBlank()) {
                            Picasso.get().load(message.profilePictureReceiver)
                                .into(holder.profile)
                        }
                    }
                }

                itemView.setOnClickListener {
                    if (message.messageReceiverId == firebaseAuth.currentUser!!.uid) {
                        user = UserModel(
                            message.messageSenderId,
                            "",
                            message.nameSender,
                            message.profilePictureSender
                        )
                    } else {
                        user = UserModel(
                            message.messageReceiverId,
                            "",
                            message.nameReceiver,
                            message.profilePictureReceiver
                        )
                    }

                    val bundle = bundleOf(USER_KEY to user, USERR_KEY to currentUser)
                    findNavController().navigate(
                        R.id.privateChatFragment, bundle
                    )
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        QueryPreferenc.setLastResultIdText((activity as MainActivity).baseContext, query)
    }
}