package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentListOfUsersBinding
import com.spongycode.blankspace.databinding.ItemListofusersBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.Constants.USERR_KEY
import com.spongycode.blankspace.util.Constants.USER_KEY
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ListOfUsersFragment: Fragment() {
    private var _binding: FragmentListOfUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var userItemBinding: ItemListofusersBinding
    private val chatViewModel = MainActivity.chatViewModel
    lateinit var currentUser: UserModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListOfUsersBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigateUp()
        }

        // there's a better way to do this
        chatViewModel.user.observe(viewLifecycleOwner, {
            currentUser = it[0]
            Log.d("user", " ti, $currentUser")
        })

        if(chatViewModel.listUsers.isEmpty()){
            chatViewModel.userLiveData.observe(viewLifecycleOwner, {
                chatViewModel.listUsers.apply {
                    addAll(it)
                    this.remove(currentUser)
                    it.toSet()
                }
                Log.d("user", "usesf: ${chatViewModel.listUsers}")
                binding.listOfUsers.adapter = ListOfUsersAdapter(chatViewModel.listUsers)
                binding.listOfUsers.addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
                binding.listOfUsers.adapter?.notifyDataSetChanged()

            })
        } else {
            binding.listOfUsers.adapter = ListOfUsersAdapter(chatViewModel.listUsers)
            binding.listOfUsers.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            binding.listOfUsers.adapter?.notifyDataSetChanged()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "List of users"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    inner class ListOfUsersAdapter(private val listUsers: List<UserModel>):
        RecyclerView.Adapter<ListOfUsersAdapter.ListOfUsersViewHolder>(){

        inner class ListOfUsersViewHolder(binding: ItemListofusersBinding):
            RecyclerView.ViewHolder(binding.root){
                internal val name: MaterialTextView = userItemBinding.name
                internal val status: MaterialTextView = userItemBinding.status
                internal val profile: CircleImageView = userItemBinding.profilePic
            }
//
//        private val diffUtil = object : DiffUtil.ItemCallback<UserModel>(){
//            override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
//                return false
//            }
//
//            override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
//                return false
//            }
//        }
//
//        val differ = AsyncListDiffer(this, diffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfUsersViewHolder {
            userItemBinding = ItemListofusersBinding.inflate(LayoutInflater.from(parent.context))
            return ListOfUsersViewHolder(userItemBinding)
        }

        override fun onBindViewHolder(holder: ListOfUsersViewHolder, position: Int) {
            val user = listUsers.get(position)
            with(holder){
                with(user){
                    holder.name.text = this.username
                    holder.status.text = this.status
                    if (this.imageUrl.isNotBlank()) {
                        Picasso.get().load(this.imageUrl)
                            .into(holder.profile)
                    }
                }

                itemView.setOnClickListener {
                    val bundle = bundleOf(USER_KEY to user, USERR_KEY to currentUser)
                    findNavController().navigate(R.id.privateChatFragment, bundle)
                }
            }
        }
        override fun getItemCount() = listUsers.size
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}