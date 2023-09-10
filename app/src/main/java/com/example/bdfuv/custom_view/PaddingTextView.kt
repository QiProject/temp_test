package com.example.bdfuv.custom_view


import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import com.example.bdfuv.R
import com.example.bdfuv.util.FontUtil


class PaddingTextView : TextView {
    val ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android"

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    )
            : super(context, attrs, defStyleAttr) {
        applyCustomFont(context, attrs!!)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    )
            : super(context, attrs, defStyleAttr, defStyleRes) {
        applyCustomFont(context, attrs!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paint = paint
        val color = paint.color
        // Draw what you have to in transparent
        // This has to be drawn, otherwise getting values from layout throws exceptions
        super.onDraw(canvas)
        // setTextColor invalidates the view and causes an endless cycle
        paint.color = color


        val layout = layout
        val text = text.toString()

//        val customFont = FontUtil.getOTF(context, "nivea_creme_initial")
//        typeface = customFont

        for (i in 0 until layout.lineCount) {
            val start = layout.getLineStart(i)
            val end = layout.getLineEnd(i)

            val line = text.substring(start, end)

            println("Line:\t$line")

            val left = layout.getLineLeft(i)
            val baseLine = layout.getLineBaseline(i)


            canvas!!.drawText(
                line,
                left + totalPaddingLeft,
                // The text will not be clipped anymore
                // You can add a padding here too, faster than string string concatenation
                (baseLine + totalPaddingTop).toFloat(),
                getPaint()
            )
        }
    }

    private fun applyCustomFont(context: Context, attrs: AttributeSet) {

        val attributeArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PaddingTextView
        )

        val fontName = attributeArray.getString(R.styleable.PaddingTextView_myfont)!!

        val customFont = FontUtil.getOTF(context, fontName)
        typeface = customFont

        attributeArray.recycle()
    }
}