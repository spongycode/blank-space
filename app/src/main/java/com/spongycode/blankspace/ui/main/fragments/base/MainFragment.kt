package com.spongycode.blankspace.ui.main.fragments.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.api.ApiInterface
import com.spongycode.blankspace.model.modelmemes.MemeList
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.ui.main.adapters.MemeRecyclerAdapter
import com.spongycode.blankspace.util.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    lateinit var apiInterface: Call<MemeList?>

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

        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(
                    requireContext(),
                    parent?.getItemAtPosition(position).toString(),
                    Toast.LENGTH_LONG
                ).show()
                fetchMemeByCategory(parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return binding.root
    }

    private fun fetchMemeByCategory(category: String) {

        if (category == "Member Edits"){
            return
        }

        when(category){
            "Random" -> apiInterface = ApiInterface.create().getMemesRandom()
            "Coding" -> apiInterface = ApiInterface.create().getMemesProgram()
            "Science" -> apiInterface = ApiInterface.create().getMemesScience()
            "Gaming" -> apiInterface = ApiInterface.create().getMemesGaming()
        }


        apiInterface.enqueue(object : Callback<MemeList?> {
            override fun onResponse(call: Call<MemeList?>, response: Response<MemeList?>) {

                if (response.body() != null) {
                    val memeList = mutableListOf<MemeModel>()
                    for (i in response.body()!!.memes!!) {
                        memeList.add(i)
                    }

                    val linearLayoutManager =
                        LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                    binding.rvMeme.layoutManager = linearLayoutManager
                    binding.rvMeme.adapter = MemeRecyclerAdapter(requireActivity(), memeList)
                    val adapter = binding.rvMeme.adapter
                    adapter?.notifyDataSetChanged()

                } else {
                    Toast.makeText(requireActivity(), "Error fetching", Toast.LENGTH_LONG).show()

                }

            }

            override fun onFailure(call: Call<MemeList?>, t: Throwable) {
                Log.d(TAG, "Error Fetching: ${t.printStackTrace()}")
                Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }
}