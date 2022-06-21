package farrukh.tracker.assignment3gps

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import farrukh.tracker.assignment3gps.MainActivity.Companion.PermissionCheck
import org.xml.sax.SAXException
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class GPSWriter {
    //private val TAG = GPSWriter::class.java.name
    var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val dateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    private val calendar = Calendar.getInstance()
    private val date = dateFormat.format(calendar.time).replace(" ","").trim()
    private val myfileName = "$date.gpx"
    private val mainActivity: MainActivity? = null
    private val context: Context? = null
    private val pathtoSD: File
    private val myfolder: File
    private val gpxFile: File


    fun writePath(file: File, gps: myGPS, context: Context?, main: MainActivity?) {
        val header =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n"
        val distance = """
             <distance>${gps.distance}</distance>
             
             """.trimIndent()
        val time = """
             <time>${MainActivity.runningTime}</time>
             
             """.trimIndent()
        //
        val minSpeed = """
             <minspeed>${gps.minSpeed}</minspeed>
             
             """.trimIndent()
        val maxSpeed = """
             <maxspeed>${gps.maxSpeed}</maxspeed>
             
             """.trimIndent()
        val averageSpeed = """
             <averagespeed>${gps.averageSpeed}</averagespeed>
             
             """.trimIndent()
        //
        val minAltitude = """
             <minaltitude>${gps.altitudeMin()}</minaltitude>
             
             """.trimIndent()
        val maxAltitude = """
             <maxaltitude>${gps.altitudeMax()}</maxaltitude>
             
             """.trimIndent()
        val gainAltitude = """
             <gainaltitude>${gps.altitudeGain()}</gainaltitude>
             
             """.trimIndent()
        val lossAltitude = """
             <lossaltitude>${gps.altitudeLoss()}</lossaltitude>
             
             """.trimIndent()
        var trackingPoints = "<trkseg>\n"
        val simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        for (location in gps.locationList) {
            trackingPoints += """<trkpt lat="${location.latitude}" lon="${location.longitude}" speed= "${location.speed}"><ele>${location.altitude}</ele><time>${
                simpleDateFormat.format(
                    Date(location.time)
                )
            }</time></trkpt>
"""
        }
        val footer = "</trkseg>\n</trk>\n</gpx>\n"
        try {
            if (!PermissionCheck(context, *PERMISSIONS)) {
                ActivityCompat.requestPermissions(mainActivity!!, PERMISSIONS, 1)
            } else {
                val fileData = header + distance + time + minSpeed + maxSpeed + averageSpeed + minAltitude + maxAltitude +
                        gainAltitude + lossAltitude + trackingPoints + footer
                val fileOutPutStream = FileOutputStream(file)
                fileOutPutStream.write(fileData.toByteArray())
                fileOutPutStream.close()
                Toast.makeText(main, "GPX recorded", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun readerGPX(path: String): myGPS {
        val gps: myGPS
        val list: MutableList<Location> = ArrayList()
        var distance = 0.0
        var speed = 0.0
        var maxSpeed = 0.0
        var minSpeed = 0.0
        var averageSpeed = 0.0
        var minAltitude = 0.0
        var maxAltitude = 0.0
        var gainAltitude = 0.0
        var lossAltitude = 0.0
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        try {
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val fileInputStream = FileInputStream(lastFileModified(path))
            val document = documentBuilder.parse(fileInputStream)
            val elementRoot = document.documentElement
            val nodelist_trkpt = elementRoot.getElementsByTagName("trkpt")
            val nodelist_ele = elementRoot.getElementsByTagName("ele")
            distance = elementRoot.getElementsByTagName("distance").item(0).textContent.toFloat()
                .toDouble()
            minSpeed = elementRoot.getElementsByTagName("minspeed").item(0).textContent.toFloat()
                .toDouble()
            maxSpeed = elementRoot.getElementsByTagName("maxspeed").item(0).textContent.toFloat()
                .toDouble()
            averageSpeed =
                elementRoot.getElementsByTagName("averagespeed").item(0).textContent.toFloat()
                    .toDouble()
            minAltitude =
                elementRoot.getElementsByTagName("minaltitude").item(0).textContent.toDouble()
            maxAltitude =
                elementRoot.getElementsByTagName("maxaltitude").item(0).textContent.toDouble()
            gainAltitude =
                elementRoot.getElementsByTagName("gainaltitude").item(0).textContent.toDouble()
            lossAltitude =
                elementRoot.getElementsByTagName("lossaltitude").item(0).textContent.toDouble()
            for (i in 0 until nodelist_trkpt.length) {
                val node = nodelist_trkpt.item(i)
                val attributes = node.attributes
                val latitude = attributes.getNamedItem("lat").textContent.toDouble()
                val longitude = attributes.getNamedItem("lon").textContent.toDouble()
                val speed = attributes.getNamedItem("speed").textContent.toDouble()
                val altitude = nodelist_ele.item(i).textContent.toDouble()
                val location = Location("GPX $i")
                location.latitude = latitude
                location.longitude = longitude
                location.altitude = altitude
                location.speed  = speed.toFloat()
                list.add(location)
            }
            fileInputStream.close()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        gps = myGPS(
            list,
            distance,
            speed,
            maxSpeed,
            minSpeed,
            averageSpeed,
            minAltitude,
            maxAltitude,
            gainAltitude,
            lossAltitude
        )
        return gps
    }

    companion object {
        fun lastFileModified(dir: String?): File? {
            val fl = File(dir)
            var choice: File? = null
            if (!fl.listFiles().isNullOrEmpty()) {
                val files = fl.listFiles { file -> file.isFile }
                var lastMod = Long.MIN_VALUE
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (file.lastModified() > lastMod) {
                            choice = file
                            lastMod = file.lastModified()
                        }
                    }
                }
            }


            return choice
        }
    }

    init {
        pathtoSD = Environment.getExternalStorageDirectory()
        myfolder = File("$pathtoSD/GPStracks")
        myfolder.mkdirs()
        gpxFile = File(myfolder, myfileName)
    }
}