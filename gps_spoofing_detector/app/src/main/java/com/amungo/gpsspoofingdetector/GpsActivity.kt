package com.amungo.gpsspoofingdetector

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_gps.*
import java.util.*

class GpsActivity : AppCompatActivity(), GeolocationData.Observer {

    private val gps: GpsReader = GpsReader()

    private var timer: Timer = Timer()
    private val firebaseProxy = FirebaseProxy()

    private var lastAnomaly: Anomaly? = null
    private lateinit var adapter: GeolocationDataAdapter
    private val geolocationData = GeolocationCommon.data

    companion object {
        private const val MINIMAL_DISTANCE = 100f
    }

    init {
        gps.gpsActivity = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)
        firebaseProxy.getAnomalies()
        setupAdapter()
        geolocationData.subscribe(this)

            tw_speed_soft.text = getString(R.string.s_no_speed)
    }

    @Suppress("UNUSED_PARAMETER") //Actually parameter is used from layout
    fun startClicked(view: View) {
        startGps()
    }

    @Suppress("UNUSED_PARAMETER") //Actually parameter is used from layout
    fun stopClicked(view: View) {
        stopGps()
    }

    @Suppress("UNUSED_PARAMETER") //Actually parameter is used from layout
    fun sendAnomaly(view: View) {
        if (gps.locationsArePresent()) {
            val loc = gps.getLastLocation()

            firebaseProxy.sendAnomaly(Anomaly(AnomalyType.ProvidersAnomaly, loc, loc, Date()))
        }
    }

    private fun setupAdapter() {
        val llm = LinearLayoutManager(this)
        val rv = findViewById<RecyclerView>(R.id.recycler_geolocation)
        rv.layoutManager = llm

        adapter = GeolocationDataAdapter(this, geolocationData)
        rv.adapter = adapter

    }

    @Suppress("UNUSED_PARAMETER") //Actually parameter is used from layout
    fun showMap(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
//        if (gps.locationsArePresent() && (gps.isAnomaly() == AnomalyType.NoAnomaly)) {
        val loc = gps.lastGenuineLocation
        loc?.let {
            intent.putExtra("latitude", it.latitude)
            intent.putExtra("longitude", it.longitude)
        }

        startActivity(intent)
    }

    private class GpsTimerTask(val activity: GpsActivity): TimerTask() {
        override fun run() {
            activity.updateGps()
        }

    }

    private fun startGps() {
        b_start_gps.visibility = View.GONE
        b_stop_gps.visibility = View.VISIBLE
        gps.readLocation()


        timer = Timer()
        timer.schedule(GpsTimerTask(this), 1000, 1000)
    }

    private fun stopGps() {
        b_start_gps.visibility = View.VISIBLE
        b_stop_gps.visibility = View.GONE

        timer.cancel()
    }

    fun updateGps() {
        Log.d("GPS_SPOOF_DETECTOR", "updateGps")
        gps.readLocation()
        indicateGps()
        if (gps.locationsArePresent()) {
            val loc = gps.getLastLocation()
            val speed = gps.calcSpeedMPS()
            runOnUiThread {
                tw_speed_hw.text = getString(R.string.s_tw_speed_hw, loc.speed)
                tw_speed_soft.text = getString(R.string.s_tw_speed_soft, speed)
            }
        }

        val anomalyType = gps.isAnomaly()


        if (anomalyType != AnomalyType.NoAnomaly) {
            val anomaly = gps.getAnomaly()
            if (lastAnomaly == null) {
                firebaseProxy.sendAnomaly(anomaly)
                lastAnomaly = anomaly
            } else {
                lastAnomaly?.let {
                    val distRes = floatArrayOf(0f, 0f, 0f)
                    Location.distanceBetween(it.lastLocation.latitude,
                            it.lastLocation.longitude,
                            anomaly.lastLocation.latitude,
                            anomaly.lastLocation.longitude, distRes)
                    val dist = distRes[0]
                    if (dist > MINIMAL_DISTANCE) {
                        firebaseProxy.sendAnomaly(anomaly)
                        lastAnomaly = anomaly
                    }
                }
            }
        }

        when (anomalyType) {

            AnomalyType.NoAnomaly -> Unit
            AnomalyType.SpeedAnomaly -> writeAnomaly("Speed anomaly")
            AnomalyType.ProvidersAnomaly -> writeAnomaly("Provider anomaly")
        }
    }

    private fun indicateGps() {
        runOnUiThread {
            switch_gps_visible.isChecked = gps.gpsCaptured
        }
    }

    private fun writeAnomaly(text: String) {
        runOnUiThread({
            tw_spoof.text = text
            tw_spoof.visibility = View.VISIBLE
        })
    }

    override fun onDataChenaged() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onElementUpdated(position: Int) {
        runOnUiThread {
            adapter.notifyItemChanged(position)
        }
    }
}
