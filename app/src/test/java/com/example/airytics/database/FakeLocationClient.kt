package com.example.airytics.database

import com.example.airytics.location.LocationClientInterface
import com.example.airytics.model.Coordinate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocationClient(private val coordinate: Coordinate): LocationClientInterface {
    override fun getCurrentLocation(): Flow<Coordinate> {
        return flowOf(coordinate)
    }
}