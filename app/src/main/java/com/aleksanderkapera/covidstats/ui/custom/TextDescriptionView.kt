package com.aleksanderkapera.covidstats.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.covidstats.R
import kotlinx.android.synthetic.main.view_text_description.view.*

class TextDescriptionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val root = View.inflate(context, R.layout.view_latest_stats, this)

        attrs?.let { attributeSet ->
            val a = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.TextDescriptionView,
                defStyleAttr,
                0
            )

            textDescription_text_main.text =
                a.getString(R.styleable.TextDescriptionView_mainText) ?: ""
            textDescription_text_main.textSize =
                a.getFloat(R.styleable.TextDescriptionView_mainSize, 32f)
            textDescription_text_description.text =
                a.getString(R.styleable.TextDescriptionView_descriptionText) ?: ""
            textDescription_text_description.textSize =
                a.getFloat(R.styleable.TextDescriptionView_descriptionSize, 32f)
        }
    }
}