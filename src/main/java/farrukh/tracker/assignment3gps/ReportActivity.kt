package farrukh.tracker.assignment3gps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import farrukh.tracker.assignment3gps.MainActivity.Companion.PermissionCheck
import java.util.concurrent.TimeUnit

class ReportActivity : Activity() {
    private var myGps: myGPS? = null
    private var gpx: GPSWriter? = null
    private val filePath = "GPSTracks"
    var PERMISSION_ALL = 1
    var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    lateinit var altitude: TextView
    lateinit var timetaken: TextView
    lateinit var speed: TextView
    lateinit var totaldistance: TextView
    private lateinit var reset:Button
    private lateinit var altitudeM:TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        // call the super class method and set the content for this activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        gpx = GPSWriter()
        myGps = myGPS(this, this)
        altitude = findViewById(R.id.altitude)
        altitudeM = findViewById<TextView>(R.id.altitude2)
        timetaken = findViewById(R.id.timetaken)
        speed = findViewById(R.id.speed)
        totaldistance = findViewById(R.id.totaldistance)
        // get access to views
        reset = findViewById<Button>(R.id.buttonReset)
       // reset = findViewById(R.id.buttonReset)

        reset.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?){
                resetButton()
            }
        })

        if (!PermissionCheck(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            addLocationListener()
        }
    }
    private fun resetButton(){
        var intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        finish()
    }

    private fun addLocationListener() {
        val path = getExternalFilesDir(filePath)?.absolutePath
        val gps = gpx!!.readerGPX(path.toString())
        altitude.setText("Max: " + gps.altitudeMax() + " m")
        altitudeM.setText("Min: "+ gps.altitudeMin() + " m")
        timetaken.text = convert(MainActivity.runningTime)
        speed.setText("Avg: " + gps.allSpeedVal + " m/s")
        totaldistance.text = gps.allDistanceVal

        val mygraph: GraphView = findViewById(R.id.graph)
        val graphArray = gps.graphPoints()
        for (i in graphArray.indices) {
            graphArray[i] = graphArray[i] / 10
        }
        mygraph.setGraphArray_double_para(graphArray)
    }



    fun convert(miliSeconds: Long): String {
        val hrs = TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt() % 24
        val min = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt() % 60
        val sec = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt() % 60
        return String.format("%02d:%02d:%02d", hrs, min, sec)
    }
}