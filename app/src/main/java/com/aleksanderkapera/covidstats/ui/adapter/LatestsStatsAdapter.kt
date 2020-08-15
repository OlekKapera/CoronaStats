package com.aleksanderkapera.covidstats.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aleksanderkapera.covidstats.databinding.ViewLatestStatsBinding
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic

class LatestStatsAdapter(var todayStats: List<AllStatusStatistic>) :
    RecyclerView.Adapter<LatestStatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewLatestStatsBinding.inflate(
            LayoutInflater.from(
                parent.context
            ),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = todayStats.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(todayStats[position])
    }

    inner class ViewHolder(private val itemBinding: ViewLatestStatsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(data: AllStatusStatistic) {
            itemBinding.data = data
        }
    }
}