package com.example.bdfuv.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable


class LEDModel(
) : BaseObservable() {

        @get:Bindable
    var ch0IR: Int? = null
        set(value) {
            field = value
//            notifyPropertyChanged(com.example.iciengineer.BR.serialNumber)
            notifyChange()
        }
    @get:Bindable
    var ch1Green: Int? = null
        set(value) {
            field = value
            notifyChange()
        }
    @get:Bindable
    var ch2UV365: Int? = null
        set(value) {
            field = value
            notifyChange()
        }
    @get:Bindable
    var ch3Red: Int? = null
        set(value) {
            field = value
            notifyChange()
        }
    @get:Bindable
    var ch4UV385: Int? = null
        set(value) {
            field = value
            notifyChange()
        }

    constructor(ch0: Int, ch1: Int, ch2: Int, ch3: Int, ch4: Int) : this() {
        this.ch0IR = ch0
        this.ch1Green = ch1
        this.ch2UV365 = ch2
        this.ch3Red = ch3
        this.ch4UV385 = ch4
    }

    override fun toString(): String {

        return ch0IR.toString() + ch1Green.toString() + ch2UV365.toString() + ch3Red.toString() + ch4UV385.toString()
    }

    fun toBytes(): ByteArray {
        return byteArrayOf(ch0IR!!.toByte(),ch1Green!!.toByte(),ch2UV365!!.toByte(),ch3Red!!.toByte(),ch4UV385!!.toByte())
    }
}