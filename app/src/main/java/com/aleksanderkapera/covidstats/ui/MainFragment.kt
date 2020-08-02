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
import com.aleksanderkapera.covidstats.ui.adapter.CountriesListAdapter
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

    private lateinit var hintsAdapter: CountriesListAdapter

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
        viewModel.countries.observe(viewLifecycleOwner, Observer {
            viewModel.hintCountries.value = it
        })

        viewModel.hintCountries.observe(viewLifecycleOwner, Observer {
            hintsAdapter.countries = it ?: emptyList()
            hintsAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutMng = LinearLayoutManager(context)
        hintsAdapter = CountriesListAdapter(viewModel.hintCountries.value ?: emptyList())

        mainFragment_recycler_hints.apply {
            layoutManager = layoutMng
            adapter = hintsAdapter
        }

        (mainFragment_search as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.getCountriesByName(query ?: "")
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.getCountriesByName(query ?: "")
                return true
            }
        })

        mainFragment_search.setOnSearchClickListener {
            mainFragment_recycler_hints.visibility = View.VISIBLE
        }

        mainFragment_search.setOnCloseListener {
            mainFragment_recycler_hints.visibility = View.GONE
            mainFragment_search.onActionViewCollapsed()
            true
        }
    }
}