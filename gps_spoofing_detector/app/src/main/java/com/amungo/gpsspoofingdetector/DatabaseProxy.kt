package com.amungo.gpsspoofingdetector

/**
 * Created by Michael Lukin on 18.02.2018.
 */

interface DatabaseProxy {
    fun connectToDB()
    fun isDbConnected(): Boolean

    fun sendAnomaly(anomaly: Anomaly)

    fun getAnomalies()
}
