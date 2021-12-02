package com.example.whatdidisay

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val expBrowseButton = findViewById<Button>(R.id.export_path_browse_button)
        expBrowseButton.setOnClickListener{
            //TODO Open directory browsing dialog
            //TODO Set input path text to the chosen directory
        }

        val audioBrowseButton = findViewById<Button>(R.id.audio_path_browse_button)
        audioBrowseButton.setOnClickListener{
            //TODO Open directory browsing dialog
            //TODO Set input path text to the chosen directory
            //TODO Save setting in shared preferences
        }

        val forwardInput = findViewById<TextView>(R.id.forward_time_input)
        forwardInput.doAfterTextChanged {
            //Save new value in shared preferences
            if (forwardInput.text.isNotEmpty()){
                editor.putInt(MainActivity.FW_TIME, forwardInput.text.toString().toInt())
                Log.d("TEST", forwardInput.text as String)
            } else {
                editor.putInt(MainActivity.FW_TIME, 0)
            }

        }
    }

    override fun onResume(){
        val sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE)
        super.onResume()

        //Load entries from shared preferences
        val forwardInput = findViewById<TextView>(R.id.forward_time_input)
        forwardInput.text = sharedPrefs.getInt(MainActivity.FW_TIME, 5).toString() //TODO Fix issue translating the value to a string
        val expLocInput = findViewById<TextView>(R.id.export_path_input)
        expLocInput.text = sharedPrefs.getString(MainActivity.EXPORT_LOC, "")
        val audioLocInput = findViewById<TextView>(R.id.audio_path_input)
        audioLocInput.text = sharedPrefs.getString(MainActivity.AUDIO_LOC, "")
    }
}