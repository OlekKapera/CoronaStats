package com.aleksanderkapera.covidstats.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import kotlinx.android.synthetic.main.item_country.view.*

class CountriesListAdapter(var countries: List<Country>) :
    RecyclerView.Adapter<CountriesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        )
    }

    override fun getItemCount(): Int = countries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = countries[position].countryName
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val text = view.countryItem_text_name as TextView
        val image = view.countryItem_image_tick as ImageView

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            image.visibility = if (image.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}