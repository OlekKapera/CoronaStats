package com.aleksanderkapera.covidstats.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.ui.adapter.CountriesListAdapter
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

            view.dialogCountries_recycler_hints.apply {
                layoutManager = layoutMng
                adapter = hintsAdapter
            }

            (view.dialogCountries_search as SearchView).setOnQueryTextListener(object :
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

            builder.setView(view)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> })
                .setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}