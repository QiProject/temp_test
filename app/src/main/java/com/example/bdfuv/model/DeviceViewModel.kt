package com.example.bdfuv.model

data class DeviceViewModel(
    var rssi:Int,
    val name:String,
    val localName:String,
    val macAddress:String
)