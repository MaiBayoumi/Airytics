package com.example.airytics.location


import com.example.airytics.model.Coordinate
import kotlinx.coroutines.flow.Flow

interface LocationClientInterface {
    fun getCurrentLocation(): Flow<Coordinate>
}