package com.example.bdfuv.custom_view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.bdfuv.R


class ThreeStateIndicator : TwoStateIndicator {
    var isHighlighted: Boolean = false
        set(value) {
            if (value) {
                centerPaint.color = ContextCompat.getColor(context, R.color.mango)
            } else {
                this.isDone = this.isDone
            }

            field = value
            invalidate()
        }


    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    )
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    )
            : super(context, attrs, defStyleAttr, defStyleRes)
}