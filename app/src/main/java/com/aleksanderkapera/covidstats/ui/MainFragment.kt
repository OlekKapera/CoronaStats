package com.aleksanderkapera.covidstats.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.databinding.FragmentMainBinding
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel
import com.google.android.material.snackbar.Snackbar

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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.exceptionCaughtEvent.observe(viewLifecycleOwner, Observer { isExceptionCaught ->
            if (isExceptionCaught) {
                Snackbar.make(
                    binding.root,
                    R.string.exception_fetch_statistics,
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.exceptionHandled()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}