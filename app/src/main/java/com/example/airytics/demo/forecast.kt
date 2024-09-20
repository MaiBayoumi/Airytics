package com.example.airytics.demo

data class forecast(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Item8>,
    val message: Int
)