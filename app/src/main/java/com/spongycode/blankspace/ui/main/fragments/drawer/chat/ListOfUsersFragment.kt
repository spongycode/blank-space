package com.spongycode.blankspace.ui.main.fragments.drawer.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentListOfUsersBinding
import com.spongycode.blankspace.databinding.ItemListofusersBinding
import com.spongycode.blankspace.model.modelChat.User
import com.spongycode.blankspace.ui.main.MainActivity
import com.squareup.picasso.Picasso

class ListOfUsersFragment: Fragment() {

    private var _binding: FragmentListOfUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: ItemListofusersBinding
    // get the current user and list of users
    private val currentUser = User()
    private val listUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListOfUsersBinding.inflate(inflater, container, false)
        // Set the adapter
        with(binding.listOfUsers){
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = ListOfUsersAdapter()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Users"
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listUsers.remove(currentUser)
        Log.d("list", "list: $listUsers")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class ListOfUsersAdapter : RecyclerView.Adapter<ListOfUsersAdapter.ListOfUsersViewHolder>() {
        inner class ListOfUsersViewHolder(binding: ItemListofusersBinding): RecyclerView.ViewHolder(binding.root){
            internal val profile = itemBinding.profilePic
            internal val name = itemBinding.name
            internal val status = itemBinding.status
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfUsersViewHolder {
            itemBinding = ItemListofusersBinding.inflate(LayoutInflater.from(parent.context))
            return ListOfUsersViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ListOfUsersViewHolder, position: Int) {
            val user = listUsers[position]
            with(holder){
                holder.name.text = user.name
                holder.status.text = user.status
                Picasso.get().load(user.profilePicture)
                    .error(R.drawable.ic_nav_account)
                    .into(holder.profile)

            }
        }

        override fun getItemCount() = listUsers.size
    }

}