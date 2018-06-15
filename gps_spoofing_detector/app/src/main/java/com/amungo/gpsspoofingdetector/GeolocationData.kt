package com.amungo.gpsspoofingdetector

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Michael Lukin on 23.05.2018.
 */

data class GeolocationItem(val name: String, var longitude: Double, var latitude: Double)

data class GeolocationData(private val data: CopyOnWriteArrayList<GeolocationItem> = CopyOnWriteArrayList()) {
    init {
//        add("gps", 60.0, 30.0)
    }

    interface Observer {
        fun onDataChenaged()
        fun onElementUpdated(position: Int)
    }

    private val observers = CopyOnWriteArrayList<Observer>()

    fun subscribe(o: Observer) {
        observers.add(o)
    }

    fun unsubscribe(o: Observer) {
        observers.remove(o)
    }

    private fun notifyDataChanged() {
        for (o in observers) {
            o.onDataChenaged()
        }
    }

    private fun notifyElementUpdated(position: Int) {
        for (o in observers) {
            o.onElementUpdated(position)
        }
    }

    fun equal(left: Double, right: Double): Boolean {
        val delta = 2e-6
        return (Math.abs(left - right) < delta)
    }

    fun add(name: String, longitude: Double, latitude: Double) {
        var found = false
        for (i in 0 until data.size) {
            if (data[i].name == name) {
                var newData = false
                if (!equal(data[i].latitude, latitude)) {
                    newData = true
                    data[i].latitude = latitude
                }
                if (!equal(data[i].longitude, longitude)) {
                    newData = true
                    data[i].longitude = longitude
                }
                if (newData) {
                    notifyElementUpdated(i)
                }
                found = true
            }
        }
        if (!found) {
            data.add(GeolocationItem(name, longitude, latitude))
            notifyDataChanged()
        }
    }

    operator fun get(position: Int): GeolocationItem = data[position]

    fun size(): Int = data.size
}