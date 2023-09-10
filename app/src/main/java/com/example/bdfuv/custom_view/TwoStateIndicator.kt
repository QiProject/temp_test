package com.example.bdfuv.custom_view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.*
import com.example.bdfuv.R

open class TwoStateIndicator : View {
    var isDone: Boolean = false
        set(value) {
            centerPaint.color = if (value) {
                ContextCompat.getColor(context, R.color.niveaBlue4)
            } else {
                ContextCompat.getColor(context, R.color.transparent)
            }
            field = value
            invalidate()
        }

    val strokeWidth = 5f

    val centerPaint: Paint = Paint()

    init {
        centerPaint.strokeWidth = 1f
        centerPaint.style = Paint.Style.FILL
        centerPaint.isAntiAlias = true
        centerPaint.color = ContextCompat.getColor(context, R.color.transparent)
    }

    val strokePaint: Paint = Paint()

    init {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.isAntiAlias = true
        strokePaint.strokeWidth = strokeWidth
        strokePaint.color = ContextCompat.getColor(context, R.color.warmGrey)
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    )
            : super(context, attrs, defStyleAttr) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.TwoStateIndicator, 0, 0
            )
            val isDone =
                typedArray
                    .getBoolean(
                        R.styleable
                            .TwoStateIndicator_isDone,
                        false
                    )
            centerPaint.color = if (isDone) {
                ContextCompat.getColor(context, R.color.niveaBlue4)
            } else {
                ContextCompat.getColor(context, R.color.transparent)
            }

            typedArray.recycle()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    )
            : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = 65
        val desiredHeight = 65

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        //Measure Width
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                widthSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                Math.min(desiredWidth, widthSize)
            else -> //Be whatever you want
                desiredWidth
        }

        //Measure Height
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                heightSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                Math.min(desiredHeight, heightSize)
            else -> //Be whatever you want
                desiredHeight
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(
            height.toFloat() / 2,
            width.toFloat() / 2,
            (height / 2).toFloat() - strokeWidth / 2,
            centerPaint
        )
        canvas?.drawCircle(height.toFloat() / 2, width.toFloat() / 2, ((height - strokeWidth) / 2), strokePaint)
    }

}