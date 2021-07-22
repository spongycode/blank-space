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
import com.spongycode.blankspace.databinding.FragmentGroupChatBinding
import com.spongycode.blankspace.databinding.LeftsidemessageBinding
import com.spongycode.blankspace.databinding.RightsidemessageBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.storage.sendGroupMessage
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.fragments.base.PhotoViewerDialog
import com.spongycode.blankspace.util.Constants.groupId
import com.spongycode.blankspace.util.gallery
import com.spongycode.blankspace.util.loadImage
import com.spongycode.blankspace.viewmodel.ChatViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class GroupChatFragment: Fragment() {

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var lMessageBinding: LeftsidemessageBinding
    private lateinit var rMessageBinding: RightsidemessageBinding
    private val chatViewModel: ChatViewModel = MainActivity.chatViewModel
    private val chatMessages = mutableListOf<ChatMessage>()
    private var sender = UserModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        chatViewModel.user.observe(viewLifecycleOwner, {
            sender = it.get(0)
        })

        receiveMessage()

//        chatViewModel.receiveGroupMessages("user-messages/group/$groupId").observe(
//            viewLifecycleOwner, {
//                chatViewModel.groupMessages.addAll(it)
//                binding.list.adapter = GroupChatAdapter(chatViewModel.groupMessages)
//                binding.list.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
//                binding.list.adapter?.notifyDataSetChanged()
//                binding.list.scrollToPosition(chatViewModel.groupMessages.size - 1)
//            }
//        )

        binding.apply {

            messageText.setText(chatViewModel.currentText)
            // only send the message if there's text entered
            // clear the textField after sending the message
            messageSend.setOnClickListener {
                if (messageText.text!!.isNotBlank()) {
                    sendGroupMessage(groupId, sender, messageText.text.toString(), ""); messageText.text?.clear()
                } else return@setOnClickListener
            }
            imageSend.setOnClickListener {
                startActivityForResult(gallery, 4)
            }
        }

        // change app bar title to receivers name
        (activity as MainActivity).supportActionBar?.title = groupId
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 4 && data != null && data.data != null) {
            loadImage(data.data!!, this, sender, null)
        }
    }

    // this will go to repo
    // this failed to go to repo, it shall remain here
    private fun receiveMessage(){
        CoroutineScope(Dispatchers.IO).launch{
            val a = Firebase.firestore
                .collection("user-messages/group/$groupId")
            a
                .orderBy("messageTime")
                .addSnapshotListener { value, error ->
                    error?.let {
                        Log.w("error", error)
                    }

                    value?.let {
                        chatMessages.clear()
                        Log.d("error", "data: ${it.documents}")
                        for (doc in it){
                            val message = doc.toObject<ChatMessage>()
                            chatMessages.add(message)
                        }
                        chatMessages.sortByDescending { it.messageTime }
                        binding.list.adapter = GroupChatAdapter(chatMessages)
                        binding.list.addItemDecoration(DividerItemDecoration((activity as MainActivity).baseContext,
                            DividerItemDecoration.VERTICAL))
                        binding.list.adapter?.notifyDataSetChanged()
                    }
                }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
////        calling this caused a bug
////        _binding = null
//    }
    override fun onPause() {
        super.onPause()
        chatViewModel.currentText = binding.messageText.text.toString()
    }

    // basic adapter
    private inner class GroupChatAdapter(private val messages: List<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        inner class PrivateChatSenderViewHolder(binding: RightsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val rMessage: MaterialTextView = binding.rMessage
            internal val rImage: ShapeableImageView = binding.rImage
        }

        inner class PrivateChatReceiverViewHolder(binding: LeftsidemessageBinding):
            RecyclerView.ViewHolder(binding.root){
            internal val lMessage: MaterialTextView = binding.lMessage
            internal val lName: MaterialTextView = binding.lName
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
//                    well, i guess i will have to build an unique message with all features, better than having three
                    this.lMessage.text = message.messageText
                    this.lName.text = message.nameSender
                    if (message.image.isNotBlank()){
                        this.lImage.visibility = View.VISIBLE
                        Log.d("image", message.image)
                        Picasso.get().load(message.image.toUri()).into(this.lImage)
                        this.lImage.setOnClickListener {
                            PhotoViewerDialog.newInstance(message.image).show(parentFragmentManager, "hello")
                        }
                        if (this.lMessage.text.isBlank()) this.lMessage.visibility = View.GONE
                        else this.lMessage.visibility = View.VISIBLE
                    } else {
                        this.lImage.visibility = View.GONE
                    }
                }

            }
        }
        override fun getItemCount() = messages.size
    }
}