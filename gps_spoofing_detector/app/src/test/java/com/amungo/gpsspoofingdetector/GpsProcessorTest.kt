package com.amungo.gpsspoofingdetector

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GpsProcessorTest {
    @Test
    fun testCalcSpeedMPS() {
        //60.093802, 30.096098
        //60.095506, 30.094746
        //211 meters

        val speed = GpsProcessor.calcSpeedMPS(
                startLatitude = 60.093802,
                startLongitute = 30.096098,
                endLatitude = 60.095506,
                endLongitude = 30.094746,
                time = 1.0
        )
        assert(speed > 200)
        assert(speed < 250)
        assertEquals(4, 2 + 2)
    }
}
