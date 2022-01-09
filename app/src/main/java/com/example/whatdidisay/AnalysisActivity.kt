package com.example.whatdidisay

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import org.json.JSONArray


class AnalysisActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analysis_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val data = generateHeatMapData()

        val startPoints = floatArrayOf(0.2f, 1f)

        // Create the gradient.
        val colors = intArrayOf(
            Color.rgb(102, 225, 0),  // green
            Color.rgb(255, 0, 0) // red
        )

        val gradient = Gradient(colors, startPoints)

        val heatMapProvider = HeatmapTileProvider.Builder()
            .data(data)
            .gradient(gradient)
            .opacity(0.5) // set opacity, default is 0.7
            .radius(50) // optional, in pixels, can be anything between 20 and 50
            .build()

        // Add the tile overlay to the map.
        googleMap?.addTileOverlay(TileOverlayOptions().tileProvider(heatMapProvider))

        // ToDo: Center Google Map over Hanover
        val hanoverLatLng = LatLng(52.37052, 9.73322)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoverLatLng, 10f))
    }

    private fun generateHeatMapData(): ArrayList<LatLng> {
        val data = ArrayList<LatLng>()        // call our function which gets json data from our asset file
        val jsonData = getJsonDataFromAsset("location_data.json")        // ensure null safety with let call
        jsonData?.let {
            // loop over each json object
            for (i in 0 until it.length()) {
                // parse each json object
                val entry = it.getJSONObject(i)
                val lat = entry.getDouble("lat")
                val lng = entry.getDouble("lon")
                data.add(LatLng(lat, lng))
            }
        }
        // ToDo: Add current location of the recording to the data
        return data
    }

    // Read the JSON file and retrieve its contents as a JSON array.
    private fun getJsonDataFromAsset(fileName: String): JSONArray? {
        return try {
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}