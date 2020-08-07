package com.aleksanderkapera.covidstats.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.databinding.FragmentMainBinding
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.ui.adapter.LatestStatsAdapter
import com.aleksanderkapera.covidstats.util.InjectorUtils
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private lateinit var mainViewModel: MainFragmentViewModel

    private val linearLayoutMng = LinearLayoutManager(context)
    private lateinit var recyclerAdapter: LatestStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(
            this,
            InjectorUtils.provideMainFragmentViewModelFactory(requireActivity())
        ).get(MainFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainBinding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater,
            R.layout.fragment_main,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mainViewModel
        }

        recyclerAdapter = LatestStatsAdapter(mainViewModel.todayStats.value ?: mutableListOf())

        mainViewModel.exceptionCaughtEvent.observe(
            viewLifecycleOwner,
            Observer { isExceptionCaught ->
                if (isExceptionCaught) {
                    Snackbar.make(
                        binding.root,
                        R.string.exception_fetch_statistics,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    mainViewModel.exceptionHandled()
                }
            })

        mainViewModel.userCountries.observe(viewLifecycleOwner, Observer {
            mainViewModel.updateTodayStats()

            if (mainViewModel.chooseCountryDialogEvent) {
                mainViewModel.updateStats()
                mainViewModel.finishCountryDialogChosen()
            }
        })

        // update hintCountries when new countries were fetched
        mainViewModel.countries.observe(viewLifecycleOwner, Observer { countries ->
            mainViewModel.hintCountries.value = countries

            if (mainViewModel.userCountries.value?.isNullOrEmpty() == true && countries.isNotEmpty())
                SharedPrefsManager.putList<Country>(
                    listOf(countries.find { it.iso2 == "PL" } ?: countries.first()),
                    R.string.prefs_chosen_countries.asString()
                )
        })

        mainViewModel.todayStats.observe(viewLifecycleOwner, Observer { todayStats ->
            recyclerAdapter.todayStats = todayStats
            recyclerAdapter.notifyDataSetChanged()

            mainViewModel.updateLastFetchedDate()
        })

        mainViewModel.statistics.observe(viewLifecycleOwner, Observer {
            mainViewModel.updateTodayStats()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainFragment_image_search.setOnClickListener {
            // Open dialog to choose countries to be displayed
            ChooseCountryDialog(mainViewModel).show(
                parentFragmentManager,
                R.string.dialog_choose_country.asString()
            )
        }

        mainFragment_recycler_latestStats.apply {
            layoutManager = linearLayoutMng
            adapter = recyclerAdapter
        }
    }
}