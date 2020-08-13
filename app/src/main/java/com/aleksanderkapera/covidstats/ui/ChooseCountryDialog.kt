package com.aleksanderkapera.covidstats.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.ui.adapter.CountriesListAdapter
import com.aleksanderkapera.covidstats.util.InjectorUtils
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.ChooseCountryDialogViewModel
import kotlinx.android.synthetic.main.dialog_choose_country.view.*

class ChooseCountryDialog(private val mode: Mode) : DialogFragment() {

    private lateinit var viewModel: ChooseCountryDialogViewModel

    private val layoutMng = LinearLayoutManager(context)
    private lateinit var hintsAdapter: CountriesListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(
            requireActivity(),
            InjectorUtils.provideChooseCountryDialogViewModelFactory(CovidStatsApp.context)
        ).get(ChooseCountryDialogViewModel::class.java)

        viewModel.getCountriesByName("")
        hintsAdapter =
            CountriesListAdapter(
                viewModel.hintCountries.value ?: emptyList(),
                mode
            )

        viewModel.hintCountries.observe(this, Observer {
            hintsAdapter.countries = it ?: emptyList()
            hintsAdapter.notifyDataSetChanged()
        })

        hintsAdapter.clickedCountries.observe(this, Observer { clickedCountries ->
            viewModel.clickedCountries.value = clickedCountries
            if (mode == Mode.WIDGET && clickedCountries.isNotEmpty()) {
                viewModel.onPositiveButtonClick()
            }
        })

        return activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_choose_country, null, false)

            view.apply {
                dialogCountries_recycler_hints.apply {
                    layoutManager = layoutMng
                    adapter = hintsAdapter
                }

                layoutParams = ViewGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                (dialogCountries_search as SearchView).setOnQueryTextListener(object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.getCountriesByName(query ?: "")

                        if (query.isNullOrEmpty())
                            viewModel.hintCountries

                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        viewModel.getCountriesByName(query ?: "")

                        if (query.isNullOrEmpty())
                            viewModel.hintCountries

                        return true
                    }
                })
            }

            builder.setView(view)

            if (mode == Mode.FULL) {
                builder.setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        SharedPrefsManager.putList<Country>(
                            hintsAdapter.clickedCountries.value?.toList() ?: listOf(),
                            R.string.prefs_chosen_countries.asString()
                        )
//                        viewModel.updateStats()
                        viewModel.onPositiveButtonClick()
                        resetHints()
                    })
                    .setNegativeButton(
                        R.string.cancel,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.cancel()
                            resetHints()
                        })
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun resetHints() {
        viewModel.hintCountries.value = viewModel.countries.value
    }

    enum class Mode {
        FULL, WIDGET
    }
}