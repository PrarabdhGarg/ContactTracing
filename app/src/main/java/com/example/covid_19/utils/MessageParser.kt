package com.example.covid_19.utils

import android.util.Log
import com.google.gson.JsonObject
import java.util.*

class MessageParser {

    companion object {
        private val TAG = "MessageParser"

        fun parseLocationMessage(message: String): JsonObject {
            val jsonObject = JsonObject()
            val stringTokenizer = StringTokenizer(message, "\n")
            jsonObject.apply {
                this.addProperty("Phone", stringTokenizer.nextToken().toLong())
                this.addProperty("Lat", stringTokenizer.nextToken().toDouble())
                this.addProperty("Long", stringTokenizer.nextToken().toDouble())
            }
            Log.d(TAG, "Parsed message = ${jsonObject.toString()}")
            return jsonObject
        }

        fun parseBluetoothMessage(message: String): JsonObject {
            val jsonObject = JsonObject()
            val stringTokenizer = StringTokenizer(message, "\n")
            jsonObject.apply {
                this.addProperty("Mac", stringTokenizer.nextToken())
                this.addProperty("Phone", stringTokenizer.nextToken())
            }
            Log.d(TAG, "Parsed Bluetooth Message = ${jsonObject.toString()}")
            return jsonObject
        }
    }
}