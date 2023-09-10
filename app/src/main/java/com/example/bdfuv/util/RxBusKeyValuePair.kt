package com.example.bdfuv.util

class RxBusKeyValuePair {
    var eventId: Int = 0
        private set
    var data: Any? = null
        private set

    constructor(eventId: Int, data: Any) {
        this.eventId = eventId
        this.data = data
    }
}