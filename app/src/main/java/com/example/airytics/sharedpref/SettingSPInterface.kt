package com.example.airytics.sharedpref

interface SettingSPInterface {
    fun writeStringToSettingSP(key: String, value: String)
    fun readStringFromSettingSP(key: String): String
    fun writeFloatToSettingSP(key: String, value: Float)
    fun readFloatFromSettingSP(key: String): Float
}