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
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.ui.adapter.CountriesListAdapter
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel
import kotlinx.android.synthetic.main.dialog_choose_country.view.*

class ChooseCountryDialog(private val viewModel: MainFragmentViewModel) : DialogFragment() {

    private val layoutMng = LinearLayoutManager(context)
    private val hintsAdapter = CountriesListAdapter(viewModel.hintCountries.value ?: emptyList())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.hintCountries.observe(this, Observer {
            hintsAdapter.countries = it ?: emptyList()
            hintsAdapter.notifyDataSetChanged()
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
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        SharedPrefsManager.putList<Country>(
                            hintsAdapter.clickedCountries.toList(),
                            R.string.prefs_chosen_countries.asString()
                        )
                        viewModel.onCountryDialogChosen()
                        resetHints()
                    })
                .setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.cancel()
                        resetHints()
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun resetHints() {
        viewModel.hintCountries.value = viewModel.countries.value
    }
}