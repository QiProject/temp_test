package com.example.bdfuv.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class NormalConnectModel : BaseObservable() {

    @get:Bindable
    var hint: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.hint)
//            notifyChange()

        }

    @get:Bindable
    var showButton: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.showButton)
//            notifyChange()
        }
}