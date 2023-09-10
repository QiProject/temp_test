package com.example.bdfuv.util

import android.content.Context
import android.graphics.Typeface

object FontUtil {

    private val sTypefaceCache = HashMap<String, Typeface>()

    fun getOTF(context: Context, font: String): Typeface {
        if (!sTypefaceCache.containsKey(font)) {
            val tf = Typeface.createFromAsset(
                context.applicationContext.assets, "font/%s.otf".format(font))
            sTypefaceCache.put(font, tf)
        }
        return sTypefaceCache[font]!!

    }

    fun getTFF(context: Context, font: String): Typeface {
        if (!sTypefaceCache.containsKey(font)) {
            val tf = Typeface.createFromAsset(
                context.applicationContext.assets, "font/%s.ttf".format(font))
            sTypefaceCache.put(font, tf)
        }
        return sTypefaceCache[font]!!

    }
}