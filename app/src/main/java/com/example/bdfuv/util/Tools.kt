package com.example.bdfuv.util

import java.text.SimpleDateFormat
import java.util.*


class Tools {
    companion object {
        private val hexArray = "0123456789ABCDEF".toCharArray()

        fun convertByteToIntSwap(b: ByteArray): Int {
            var value = 0
            for (i in b.indices) {
                val n = (if (b[i] < 0) b[i].toInt() + 256 else b[i].toInt()) shl 8 * i
                value += n
            }
            return value
        }


        fun convertByteToInt(b: ByteArray): Int {
            var value = 0
            val reversedB = b.reversedArray()
            for (i in reversedB.indices) {
                val n = (if (reversedB[i] < 0) reversedB[i].toInt() + 256 else reversedB[i].toInt()) shl 8 * i
                value += n
            }
            return value
        }


        fun bytesToHex(bytes: ByteArray): String {
//            val hexChars = CharArray(bytes.size * 2)
//            for (j in bytes.indices) {
//                val v = (bytes[j] and 0xFF.toByte()).toInt()
//
//                hexChars[j * 2] = hexArray[v ushr 4]
//                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
//            }

            var s = ""
            for (b in bytes) {
                s += String.format("%02X", b)
            }

//            return String(hexChars)
            return s
        }

        fun getDateFileName(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
            return format.format(date)
        }

        fun getDateString(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return format.format(date)
        }

    }
}