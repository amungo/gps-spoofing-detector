package com.amungo.gpsspoofingdetector

import android.location.Location
import java.util.*

/**
* Created by Michael Lukin on 17.02.2018.
*/



data class Anomaly (
        val type: AnomalyType,
        val lastLocation: Location,
        val anomalyGpsLocation: Location,
        val timeStamp: Date)

data class LocalAnomaly (
    val type: AnomalyType,
    val lastLocation: Position,
    val anomalyGpsLocation: Position,
    val timeStamp: Date)
