package com.aleksanderkapera.covidstats.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.databinding.ViewLatestStatsBinding
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic

class LatestStatsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var binding: ViewLatestStatsBinding

    init {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(
            inflater, R.layout.view_latest_stats, this, true
        )
    }

    /**
     * Setting data binding variable
     */
    fun setData(data: AllStatusStatistic) {
        binding.data = data
    }
}