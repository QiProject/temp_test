package com.example.bdfuv.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class HistoryDeviceModel(
    @PrimaryKey var macAddress: String = ""

) : RealmObject()