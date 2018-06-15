package com.amungo.gpsspoofingdetector

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Michael Lukin on 27.03.2018.
 */

object AnomalyCache {
    val data = CopyOnWriteArrayList<LocalAnomaly>()
}
