package com.example.airytics.network

import com.example.airytics.utilities.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHepler {
    private val retrofitInstance: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofitInstance.create(ApiService::class.java)
}