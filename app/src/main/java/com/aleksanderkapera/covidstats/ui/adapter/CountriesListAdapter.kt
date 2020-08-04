package com.aleksanderkapera.covidstats.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.android.synthetic.main.item_country.view.*

class CountriesListAdapter(var countries: List<Country>) :
    RecyclerView.Adapter<CountriesListAdapter.ViewHolder>() {

    val clickedCountries = mutableSetOf<Country>()

    init {
        SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
            ?.let { countries ->
                clickedCountries.addAll(countries)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        )
    }

    override fun getItemCount(): Int = countries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countries[position]
        holder.text.text = country.countryName

        holder.image.visibility = if (country in clickedCountries) View.VISIBLE else View.GONE
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val text = view.countryItem_text_name as TextView
        val image = view.countryItem_image_tick as ImageView

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (image.visibility == View.VISIBLE) {
                clickedCountries.removeIf { it.countryName == text.text }
                image.visibility = View.GONE
            } else {
                clickedCountries.add(countries.find { it.countryName == text.text }
                    ?: throw Exception("No country in database!"))
                image.visibility = View.VISIBLE
            }
        }
    }
}