package com.example.bdfuv.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionModel(
    @PrimaryKey override var index: Int = 0,

    override var name: String = "",

    override var isChecked: Boolean = true
) : RealmObject(), CheckEditableInterface