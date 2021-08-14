package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dbottillo.notionalert.feature.home.databinding.FragmentDetailHomeBinding
import com.dbottillo.notionalert.viewBinding

class HomeDetailFragment : Fragment(R.layout.fragment_detail_home) {

    private val binding by viewBinding(FragmentDetailHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_HomeDetailFragment_to_HomeFragment)
        }
    }
}
