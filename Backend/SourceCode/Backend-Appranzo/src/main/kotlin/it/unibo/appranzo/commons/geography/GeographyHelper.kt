package it.unibo.appranzo.commons.geography

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GeographyHelper {
    companion object Helper{
        const val EARTH_RADIUS = 6371
          fun findDelta(distance: Double, userLatitude: Double): Coordinates {
            val ONE_DEGREE_MEDIUM_DISTANCE = 111.0
            val deltaLatitude =distance/ONE_DEGREE_MEDIUM_DISTANCE
            val deltaLongitude = distance/(ONE_DEGREE_MEDIUM_DISTANCE* cos(Math.toRadians(userLatitude)))
            return Coordinates(deltaLatitude, deltaLongitude)
        }

        fun haversineFormula(fromLatitude: Double, fromLongitude: Double,toLatitude: Double, toLongitude: Double) : Double{
            val radiusDistanceLat : Double = Math.toRadians(toLatitude-fromLatitude)
            val radiusDistanceLong : Double = Math.toRadians(toLongitude-fromLongitude)

            val radiansFromLatitude = Math.toRadians(fromLatitude)
            val radiansToLatitude = Math.toRadians(toLatitude)

            val hValue = (sin(radiusDistanceLat / 2).pow(2)) +
                    (sin(radiusDistanceLong / 2).pow(2))*
                            cos(radiansFromLatitude)*
                            cos(radiansToLatitude)

            val arcSin = 2* EARTH_RADIUS * (asin(sqrt(hValue)))
            return arcSin

        }
    }
}