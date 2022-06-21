package farrukh.tracker.assignment3gps

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var para_1: TextView
    private lateinit var para_3: TextView
    private lateinit var start_button: Button
    private lateinit var stop_button: Button
    private val context: Context? = null
    private var mygps: myGPS? = null
    private var myWriter: GPSWriter? = null
    private val filePath = "GPSTracks"
    private var gpsFile: File? = null
    var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // instruction on how to use app
        para_1 = findViewById(R.id.p_1);
        para_3 = findViewById(R.id.p_3);
        para_1.setText(R.string.paragraph_1);
        para_3.setText(R.string.paragraph_3);
        mygps = myGPS(this, this)
        myWriter = GPSWriter()

        //  ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        if (!PermissionCheck(this, *PERMISSIONS)) {
            // ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            checkPermissionRequest()
        }

        // get access to views
        start_button = findViewById<Button>(R.id.buttonStart)
        stop_button = findViewById<Button>(R.id.buttonStop)

        start_button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?){
                startListening()
            }
        })

        stop_button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                stopListening()

            }
        })
    }

    private fun stopListening() {
        mygps!!.stopLocation()
        runningTime = System.currentTimeMillis() - startingTime
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        gpsFile = File(getExternalFilesDir(filePath), df.format(Date()) + ".xml")
        myWriter!!.writePath(gpsFile!!, mygps!!, context, this )
        var intent: Intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
    }

    private fun startListening() {
        startingTime = System.currentTimeMillis()
        mygps!!.listenLocation()
        Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show()
    }


    fun checkPermissionRequest() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ))
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Do something, when permissions are not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                // If we should give explanation of requested permissions
                // Show an alert dialog here with request explanation
                val builder = AlertDialog.Builder(
                    this
                )
                builder.setMessage("Permissions are required for the app to function.")
                builder.setTitle("Permissions")
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS,
                        PERMISSION_ALL
                    )
                }
                builder.setNeutralButton("Cancel", null)
                val dialog = builder.create()
                dialog.show()
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    PERMISSION_ALL
                )
            }
        } else {
            // Do something, when permissions are already granted
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        }
    }




    companion object {

        const val PERMISSION_ALL = 1
        var startingTime: Long = 0
        var runningTime: Long = 0
        fun PermissionCheck(context: Context?, vararg permissions: String?): Boolean {
            if (context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context,permission!!) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }




    }
}