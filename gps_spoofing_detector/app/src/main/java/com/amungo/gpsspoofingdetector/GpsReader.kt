package com.amungo.gpsspoofingdetector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import java.io.FileDescriptor
import java.nio.BufferUnderflowException
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
* Created by Michael Lukin on 13.02.2018.
*/

class GpsReader: Service(), LocationListener {

    var gpsActivity: GpsActivity? = null

    companion object {
        private const val MIN_TIME_BW_UPDATES = (1000 * 10 * 1).toLong() // 10 seconds
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f // 10 meters

        private const val sizeLocations = 10
    }

    private var gpsIsEnabled = false
    private var networkIsEnabled = false
    private var gpsStarted = false
    private val locations = ConcurrentLinkedDeque<LocationPair>()

    data class LocationPair(val gpsLocation: Location, val networkLocation: Location, val timeStamp: Date)

    var gpsCaptured = false
    var curPosition: Location? = null

    var lastGenuineLocation: Location? = null
        private set


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        if (provider != null) {
            Log.d(this.javaClass.name, provider)
        }
    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {
        gpsStarted = false
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            addLocationPair(LocationPair(location, location, Date()))
        }
    }

    class Binder: IBinder {
        override fun getInterfaceDescriptor(): String {
            return ""
        }

        override fun isBinderAlive(): Boolean {
            return true
        }

        override fun linkToDeath(recipient: IBinder.DeathRecipient?, flags: Int) {

        }

        override fun queryLocalInterface(descriptor: String?): IInterface {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun transact(code: Int, data: Parcel?, reply: Parcel?, flags: Int): Boolean {
            return false
        }

        override fun dumpAsync(fd: FileDescriptor?, args: Array<out String>?) {
            return
        }

        override fun dump(fd: FileDescriptor?, args: Array<out String>?) {
            return
        }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient?, flags: Int): Boolean {
            return false
        }

        override fun pingBinder(): Boolean {
            return false
        }

    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    private fun hasFineLocationPermission(): Boolean {
        gpsActivity?.let {
            return ContextCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun requestFineLocationPermission() {
        if (gpsActivity != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(gpsActivity!!, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRequestPermissionRationale("We need fine location permission",
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                ActivityCompat.requestPermissions(gpsActivity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
        }
    }

    private fun hasCoarseLocationPermimssion(): Boolean {
        if (gpsActivity != null) {
            return ContextCompat.checkSelfPermission(gpsActivity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun requestCoarseLocationPermission() {
        if (gpsActivity != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(gpsActivity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showRequestPermissionRationale("We need fine location permission",
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                ActivityCompat.requestPermissions(gpsActivity!!, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            }
        }
    }

    /**
     * If the user has declined the permission before, we have to explain that the app needs this
     * permission.
     */
    private fun showRequestPermissionRationale(permission: String, message: String) {
        if (gpsActivity == null) {
            return
        }
        val dialog = AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.requestPermissions(gpsActivity!!,
                            arrayOf(permission), 0)
                }
                .create()
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    fun read() {
        try {
            var permissionsOk = false
            if (Build.VERSION.SDK_INT < 23) {
                permissionsOk = true
            } else {
                if (!hasFineLocationPermission()) {
                    requestFineLocationPermission()
                }

                if (!hasCoarseLocationPermimssion()) {
                    requestCoarseLocationPermission()
                }
            }

            if (hasFineLocationPermission() && hasCoarseLocationPermimssion()) {
                permissionsOk = true
            }

            if (permissionsOk) {
                gpsActivity?.let {
                    val lm = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    for (prov in lm.allProviders) {
                        val loc: Location? = lm.getLastKnownLocation(prov)
                        loc?.let {
                            GeolocationCommon.data.add(prov, longitude = it.longitude, latitude = it.latitude)
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readLocation() {
        read()
        if (gpsActivity == null) {
            return
        }
        val lm = gpsActivity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsNow = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkIsEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val passiveIsEnabled = lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
        val locationGpsStamp: Location
        val locationNetworkStamp: Location
        try {
            var permissionsOk = false
            if (Build.VERSION.SDK_INT < 23) {
                permissionsOk = true
            } else {
                if (!hasFineLocationPermission()) {
                    requestFineLocationPermission()
                }

                if (!hasCoarseLocationPermimssion()) {
                    requestCoarseLocationPermission()
                }
            }

            if (hasFineLocationPermission() && hasCoarseLocationPermimssion()) {
                permissionsOk = true
            }

            if (gpsNow) {
                if (permissionsOk) {
                    try {
                        if (!gpsStarted) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                            gpsStarted = true
                        }
                        val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (gpsLoc == null) {
                            gpsCaptured = false
                            return
                        } else {
                            gpsCaptured = true
                            locationGpsStamp = gpsLoc

                        }

                        val timeStamp = Date()

                        if (networkIsEnabled) {
                            locationNetworkStamp = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            val sample = LocationPair(locationGpsStamp, locationNetworkStamp, timeStamp)
                            addLocationPair(sample)
                        } else {
                            val sample = LocationPair(locationGpsStamp, locationGpsStamp, timeStamp)
                            addLocationPair(sample)
                        }

                        gpsIsEnabled = gpsNow
                    } catch (e: SecurityException) {
                        gpsActivity?.runOnUiThread({
                            Toast.makeText(this, getString(R.string.s_gps_denied), Toast.LENGTH_LONG).show()
                        })
                        e.printStackTrace()
                        Log.e(this.javaClass.name, getString(R.string.s_gps_denied))
                    }
                } else {
                    gpsStarted = false
                }
            }
            else {
                if (networkIsEnabled) {
                    if (permissionsOk) {
                        try {
                            curPosition = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        } catch (e: SecurityException) {
                            gpsActivity?.runOnUiThread({
                                Toast.makeText(gpsActivity, getString(R.string.s_gps_denied), Toast.LENGTH_LONG).show()
                            })
                            e.printStackTrace()
                            Log.e(this.javaClass.name, getString(R.string.s_gps_denied))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addLocationPair(sample: LocationPair) {
        locations.add(sample)
        if (locations.size > sizeLocations) {
            locations.removeFirst()
        }
        if (isAnomaly() == AnomalyType.NoAnomaly) {
            lastGenuineLocation = sample.gpsLocation
        } else {
            lastGenuineLocation = sample.networkLocation
        }
    }

    fun locationsArePresent(): Boolean {
        return !locations.isEmpty()
    }

    fun getLastLocation(): Location {
        if (locations.isEmpty()) {
            throw BufferUnderflowException()
        }
        return locations.last.gpsLocation
    }

    fun calcSpeedMPS(): Double {
        val size = locations.size
        if (size >= 2) {
            val ls = locations.elementAt(size - 2)
            val le = locations.last
            val dt = (le.timeStamp.time - ls.timeStamp.time) / 1000 //milliseconds -> seconds

            return GpsProcessor.calcSpeedMPS(
                    startLatitude =  ls.gpsLocation.latitude,
                    startLongitute = ls.gpsLocation.longitude,
                    endLatitude = le.gpsLocation.latitude,
                    endLongitude = le.gpsLocation.longitude,
                    time = dt.toDouble()
                    )
        }
        return 0.0
    }

    fun getAnomaly(): Anomaly {
        val anomalyType = isAnomaly()
        lateinit var lastLocation: Location
        lateinit var anomalyLocation: Location
        lateinit var timeStamp: Date

        when (anomalyType) {
            AnomalyType.NoAnomaly -> {
                lastLocation = Location("")
                anomalyLocation = Location("")
                timeStamp = Date()
            }
            AnomalyType.SpeedAnomaly -> {
                assert(locations.size >= 2) //Otherwise we could not calculate our speed
                anomalyLocation = locations.last.gpsLocation
                lastLocation = locations.elementAt(locations.size - 2).gpsLocation
                timeStamp = locations.last.timeStamp
            }
            AnomalyType.ProvidersAnomaly -> {
                val loc = locations.last
                anomalyLocation = loc.gpsLocation
                lastLocation = loc.networkLocation
                timeStamp = locations.last.timeStamp
            }
        }

        return Anomaly(anomalyType, lastLocation, anomalyLocation, timeStamp)
    }

    fun isAnomaly(): AnomalyType {
        val speed = calcSpeedMPS()
        if (speed > 1000) { //
            return AnomalyType.SpeedAnomaly
        }

        if (!locations.isEmpty()) {
            val (lastGps, lastNetwork) = locations.last
            val dist = FloatArray(2)
            Location.distanceBetween(lastGps.latitude, lastGps.longitude, lastNetwork.latitude, lastNetwork.longitude, dist)

            if (dist[0] > 10000) { //10 kilometers
                return AnomalyType.ProvidersAnomaly
            }
        }
        return AnomalyType.NoAnomaly
    }
}