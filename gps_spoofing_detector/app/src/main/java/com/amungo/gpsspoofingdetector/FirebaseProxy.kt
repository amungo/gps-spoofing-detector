package com.amungo.gpsspoofingdetector

import android.util.Log
import com.google.firebase.database.*
import java.util.*


/**
 * Created by Michael Lukin on 17.02.2018.
 */

class FirebaseProxy : DatabaseProxy {
    companion object {
        const val ANOMALY_NAME = "anomaly"
        const val TAG = "FirebaseProxy"
    }

    private val anomalyBaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val anomalyListRef = anomalyBaseRef.child(ANOMALY_NAME)


    override fun connectToDB() {

    }

    override fun isDbConnected(): Boolean {
        return true
    }

    override fun sendAnomaly(anomaly: Anomaly) {
        anomalyBaseRef.push().setValue(anomaly)
    }

    override fun getAnomalies() {
        anomalyBaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) {
                val str = dataSnapshot?.toString()
                Log.d(TAG, "onChildAdded:" + dataSnapshot?.getKey() + str)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {
                val str = dataSnapshot?.toString()
                Log.d(TAG, "onChildAdded:" + dataSnapshot?.getKey() + str)
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {
                val str = dataSnapshot?.toString()
                Log.d(TAG, "onChildAdded:" + dataSnapshot?.getKey() + str)
                dataSnapshot?.let {
                    val anomaly = it.value as HashMap<*, *>


                    Log.d(TAG, anomaly.toString())

                    val lastLatitude = it.child("lastLocation/latitude").value as Double
                    val lastLongitude = it.child("lastLocation/longitude").value as Double
                    val lastPos = Position(lastLatitude, lastLongitude, 0.0, 0.0)

                    val anomalyLatitude = it.child("anomalyGpsLocation/latitude").value as Double
                    val anomalyLongitude = it.child("anomalyGpsLocation/longitude").value as Double
                    val anomalyPos = Position(anomalyLatitude, anomalyLongitude, 0.0, 0.0)

                    //TODO: add proper date and AnomalyType
                    val localAnomaly = LocalAnomaly(AnomalyType.ProvidersAnomaly, lastPos, anomalyPos, Date())
                    AnomalyCache.data.add(localAnomaly)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            }

            override fun onCancelled(databaseError: DatabaseError) {}

        })
    }
}