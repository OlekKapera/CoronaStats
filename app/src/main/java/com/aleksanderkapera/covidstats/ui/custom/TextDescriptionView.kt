package com.aleksanderkapera.covidstats.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.covidstats.R
import kotlinx.android.synthetic.main.view_text_description.view.*

class TextDescriptionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_text_description, this)

        attrs?.let { attributeSet ->
            val a = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.TextDescriptionView,
                defStyleAttr,
                0
            )

            val mainSize = a.getDimension(R.styleable.TextDescriptionView_mainSize, -1f)
            val descSize = a.getDimension(R.styleable.TextDescriptionView_descriptionSize, -1f)

            textDescription_text_main.text =
                a.getString(R.styleable.TextDescriptionView_mainText) ?: ""
            textDescription_text_description.text =
                a.getString(R.styleable.TextDescriptionView_descriptionText) ?: ""

            if (mainSize != -1f)
                textDescription_text_main.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mainSize
                )

            if (descSize != -1f)
                textDescription_text_description.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    descSize
                )
        }
    }
}