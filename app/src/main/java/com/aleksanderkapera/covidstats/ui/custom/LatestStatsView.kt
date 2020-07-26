package com.aleksanderkapera.covidstats.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.android.synthetic.main.view_latest_stats.view.*

class LatestStatsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_latest_stats, this)

        attrs?.let {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.LatestStatsView,
                defStyleAttr,
                0
            )

            val new = a.getInteger(R.styleable.LatestStatsView_newCases, 0)
            val deaths = a.getInteger(R.styleable.LatestStatsView_deaths, 0)
            val recovered = a.getInteger(R.styleable.LatestStatsView_recovered, 0)
            val countryName =
                a.getString(R.styleable.LatestStatsView_countryName) ?: R.string.error.asString()
            val date = a.getString(R.styleable.LatestStatsView_date) ?: ""

            latestStats_textDescription_new.setMainText(new.toString())
            latestStats_textDescription_deaths.setMainText(deaths.toString())
            latestStats_textDescription_recovered.setMainText(recovered.toString())
            latestStats_text_country.text = countryName
            latestStats_text_date.text = date
        }
    }
}