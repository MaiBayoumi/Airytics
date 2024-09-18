package com.example.airytics.network

interface SettingSPInterface {
    fun writeStringToSettingSP(key: String, value: String)
    fun readStringFromSettingSP(key: String): String
    fun writeFloatToSettingSP(key: String, value: Float)
    fun readFloatFromSettingSP(key: String): Float
}