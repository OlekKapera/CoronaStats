package com.aleksanderkapera.covidstats.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.databinding.FragmentMainBinding
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.ui.adapter.CountriesListAdapter
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_main.*

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

        // update hintCountries when new countries were fetched
        viewModel.countries.observe(viewLifecycleOwner, Observer { countries ->
            viewModel.hintCountries.value = countries

            if (viewModel.userCountries.isNullOrEmpty() and countries.isNotEmpty())
                SharedPrefsManager.putList<Country>(
                    listOf(countries.find { it.iso2 == "PL" } ?: countries.first()),
                    R.string.prefs_chosen_countries.asString()
                )
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainFragment_image_search.setOnClickListener {
            // Open dialog to choose countries to be displayed
            ChooseCountryDialog(viewModel).show(
                parentFragmentManager,
                R.string.dialog_choose_country.asString()
            )
        }

//        mainFragment_search.setOnCloseListener {
//            mainFragment_recycler_hints.visibility = View.GONE
//            mainFragment_search.onActionViewCollapsed()
//            true
//        }
    }
}