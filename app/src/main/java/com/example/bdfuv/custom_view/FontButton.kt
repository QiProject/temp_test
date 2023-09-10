package com.example.bdfuv.custom_view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.Button
import com.example.bdfuv.util.FontUtil


class FontButton : Button {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    )
            : super(context, attrs, defStyleAttr) {
        applyCustomFont(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    )
            : super(context, attrs, defStyleAttr, defStyleRes) {
        applyCustomFont(context)
    }


    private fun applyCustomFont(context: Context) {
        val customFont = FontUtil.getOTF(context, "nivea_bold_text_screen")
        typeface = customFont
    }
}