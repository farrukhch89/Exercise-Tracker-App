package farrukh.tracker.assignment3gps


import android.Manifest
import android.location.LocationListener
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.ArrayList

class myGPS : LocationListener {
    private var context: Context? = null
    private val mainActivity: MainActivity? = null
    var checkGPS = false
    var canGetLocation = false
    var activity: Activity? = null
    protected var locationManager: LocationManager? = null
    private val list: MutableList<Location>
    val speedList = ArrayList<Double>()


    var distance = 0.0
    private var latitude = 0.0
    private var longitude = 0.0
    private var altitude = 0.0
    private var speed = 0.0
    private var minAltitude = 0.0
    private var maxAltitude = 0.0
    private var gainAltitude = 0.0
    private var lossAltitude = 0.0
    private var getAltitude: String? = null
    var maxSpeed = 0.0
    var minSpeed = 0.0
    var averageSpeed = 0.0
    private var getSpeed: String? = null
    private var getDistance: String? = null

    constructor(context: Context, activity: Activity?) {
        this.context = context
        this.activity = activity
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        list = ArrayList()
    }

    constructor(
        list: MutableList<Location>,
        distanceTravelled: Double,
        speed:Double,
        maxSpeed: Double,
        minSpeed: Double,
        averageSpeed: Double,
        minAltitude: Double,
        maxAltitude: Double,
        gainAltitude: Double,
        lossAltitude: Double
    ) {
        this.list = list
        distance = distanceTravelled
        this.speed = speed
        this.maxSpeed = maxSpeed
        this.minSpeed = minSpeed
        this.averageSpeed = averageSpeed
        this.minAltitude = minAltitude
        this.maxAltitude = maxAltitude
        this.gainAltitude = gainAltitude
        this.lossAltitude = lossAltitude
    }

    fun listenLocation() {
        checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!checkGPS) {
            // No network provider is enabled
        } else {
            canGetLocation = true
        }
        if (checkGPS) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    mainActivity!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    mainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0.toFloat(),
                    this
                )
            }
        }
    }

    fun stopLocation() {
        if (locationManager != null) locationManager!!.removeUpdates(this)
        //analyze data here, create method to return all values
        computeAltitude()
        computeSpeed()
        computeDistance()
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle("GPS settings")
        alertDialog.setMessage("GPS is not enabled. This app requires GPS permissions to function. \nDo you want to go to settings menu and enable it?")
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context!!.startActivity(intent)
        }
        alertDialog.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        alertDialog.show()
    }


    override fun onLocationChanged(location: Location) {
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            altitude = location.altitude
            speed = location.speed.toDouble()
            list.add(location)
            speedList.add(location.speed.toDouble())
            val info = """
                Location Changed: $latitude
                $longitude
                $altitude
                ${location.speed}
                ${location.time / 1000}
                """.trimIndent()
            printLocation(latitude, longitude)
        }
    }
    fun printLocation(latitude: Double, longitude: Double){
        Toast.makeText(
            context,
            "Latitude " + latitude + "\nLongitude " + longitude + "\nSpeed: " + speed,
            Toast.LENGTH_SHORT
        ).show()
    }



    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}


    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {
        showSettingsAlert()
    }


    val locationList: List<Location>
        get() = list

    private fun distanceBetweenPoints(l1: Location, l2: Location): Double {
        return l1.distanceTo(l2).toDouble()
    }


    fun computeDistance() {
        if (list.size > 1) {
            for (i in 0 until list.size - 1) distance += distanceBetweenPoints(list[i], list[i + 1])
        }
    }


    val allDistanceVal: String
        get() {
            getDistance = String.format("%.2f", distance) + " m"
            return getDistance ?:""
        }

    fun altitudeMin(): Double {
        return minAltitude
    }


    fun altitudeMax(): Double {
        return maxAltitude
    }

    fun altitudeGain(): Double {
        return gainAltitude
    }

    fun altitudeLoss(): Double {
        return lossAltitude
    }


    val allAltitudeVal: String
        get() {
            getAltitude = """Max: ${String.format("%.2f", altitudeMax())} m
            Min: ${String.format("%.2f", altitudeMin())} m
            """
            return getAltitude ?:""
        }


    private fun computeAltitude() {
        if (list.size > 1) {
            var temp: Double
            var tempDifference: Double
            minAltitude = list[0].altitude
            maxAltitude = list[0].altitude
            for (i in 1 until list.size) {
                temp = list[i].altitude
                tempDifference = getDifferenceBetweenAltitude(list[i - 1], list[i])
                if (temp < minAltitude) {
                    minAltitude = temp
                }
                if (temp > maxAltitude) {
                    maxAltitude = temp
                }
                if (tempDifference > 0) {
                    gainAltitude = gainAltitude + tempDifference
                } else if (tempDifference < 0) {
                    lossAltitude = lossAltitude + tempDifference
                }
            }
        }
    }

     // Get altitude difference between two coordinates
    private fun getDifferenceBetweenAltitude(location1: Location, location2: Location): Double {
        return location2.altitude - location1.altitude
    }

    val allSpeedVal: String
        get() {
            getSpeed = maxSpeed.toString()
            return getSpeed as String
        }


      //Calculate speed between two coordinates.
     // Checks if the hasSpeed method contains the speed componenet, if it does then that is saved else,
     // speed is calculated between two coordiantes

    private fun getSpeedBetweenLocation(location1: Location, location2: Location): Double {
        return if (location1.hasSpeed() && location2.hasSpeed()) {
            if (location1.speed > location2.speed) {
                location1.speed.toDouble()
            } else if (location1.speed < location2.speed) {
                location2.speed.toDouble()
            } else {
                location1.speed.toDouble()
            }
        } else {
            val distanceBetweenLocation = distanceBetweenPoints(location1, location2)
            val timeBetweenLocations = location2.time - location1.time
            calculateSpeed(distanceBetweenLocation, timeBetweenLocations)
        }
    }


    private fun calculateSpeed(distance: Double, time: Long): Double {
        return distance / time
    }

    fun computeSpeed() {
        if (list.size > 1) {
            var temp: Double
            var totalSpeed = 0.0
            maxSpeed = 0.0
            minSpeed = getSpeedBetweenLocation(list[0], list[1])
            for (i in 0 until list.size - 1) {
                temp = getSpeedBetweenLocation(list[i], list[i + 1])
                totalSpeed = totalSpeed + temp
                if (temp < minSpeed) {
                    minSpeed = temp
                }
                if (temp > maxSpeed) {
                    maxSpeed = temp
                }
            }
            averageSpeed = totalSpeed / list.size
        }
    }


     // Returns array with altitude values that will be used to draw graph

    fun graphPoints(): DoubleArray {
        val myPoint = DoubleArray(list.size)
        for (i in list.indices) {
            myPoint[i] = list[i].speed.toDouble()
        }
        return myPoint
    }


}