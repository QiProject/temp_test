package com.example.bdfuv.model

import java.util.*


data class MeasureDataModel(
    val date: Date,
    val area:String,
    val UV1:Int,
    val UV2:Int,
    val redness:Int,
    val melanin:Int,
    val temperature:Float,
    val humidity:Int
)