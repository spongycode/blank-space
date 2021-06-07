package com.spongycode.blankspace.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.ui.main.ApiInterface
import com.spongycode.blankspace.ui.main.MemeList
import com.spongycode.blankspace.ui.main.MemeModel
import com.spongycode.blankspace.ui.main.MemeRecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)


        val apiInterface = ApiInterface.create().getMemes()
        apiInterface!!.enqueue(object : Callback<MemeList?> {
            override fun onResponse(call: Call<MemeList?>, response: Response<MemeList?>) {

                if (response?.body() != null) {
                    val memeList = mutableListOf<MemeModel>()

                    for (i in response.body()!!.memes!!) {
                        memeList.add(i)
                        Toast.makeText(requireActivity(), i?.title.toString(), Toast.LENGTH_LONG).show()
                    }

                    val linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                    binding.rvMeme?.layoutManager = linearLayoutManager
                    binding.rvMeme?.adapter = MemeRecyclerAdapter(requireActivity(), memeList)
                    val adapter = binding.rvMeme?.adapter
                    adapter?.notifyDataSetChanged()

                } else {
                    Toast.makeText(requireActivity(), "Error fetching", Toast.LENGTH_LONG).show()

                }

            }

            override fun onFailure(call: Call<MemeList?>, t: Throwable) {
                Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }
        })



        return binding.root
    }
}