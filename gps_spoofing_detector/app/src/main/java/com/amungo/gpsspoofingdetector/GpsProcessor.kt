package com.amungo.gpsspoofingdetector

/**
 * Created by Michael Lukin on 07.06.2018.
 */

class GpsProcessor {
    companion object {
        /**
         * Calculate speed in meters per seconds.
         * We want to calculate speed manually to detect GPS anomalies.
         */
        fun calcSpeedMPS(startLatitude: Double, startLongitute: Double, endLatitude: Double, endLongitude: Double, time: Double): Double {
            if (time == 0.0) {
                return 0.0
            }

            val dist = distanceMeters(
                    startLatitude = startLatitude,
                    startLongitude = startLongitute,
                    endLatitude = endLatitude,
                    endLongitude = endLongitude
            )

            return dist / time
        }

        /**
         * Calculate distance between two coordinates on Earth.
         * See https://subversivebytes.wordpress.com/2013/02/23/java-calculate-distance-between-2-points-on-earth/
         */
        private fun distanceMeters(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Double {
            val theta = startLongitude - endLongitude
            var dist = Math.sin(deg2rad(startLatitude)) * Math.sin(deg2rad(endLatitude)) + Math.cos(deg2rad(startLatitude)) * Math.cos(deg2rad(endLatitude)) * Math.cos(deg2rad(theta))
            dist = Math.acos(dist)
            dist = rad2deg(dist)
            dist = dist * 60.0 * 1.1515 * 1609.344 //meters
            return dist
        }


        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        private fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }
    }

}
