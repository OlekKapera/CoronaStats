package com.example.covidstats.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.covidstats.R
import com.example.covidstats.databinding.FragmentMainBinding
import com.example.covidstats.viewmodel.MainFragmentViewModel

class MainFragment : Fragment() {

    private val viewModel: MainFragmentViewModel by lazy {
        val activity = requireNotNull(this.activity)

        ViewModelProviders.of(
            this,
            MainFragmentViewModel.MainFragmentViewModelFactory(activity.application)
        )
            .get(MainFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}