package com.example.whatdidisay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val EXPORT_LOC = "exportLoc" //Loc stands for location
        const val AUDIO_LOC = "audioLoc"
        const val FW_TIME = "fwTime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recordingButton = findViewById<Button>(R.id.cancel_button)
        recordingButton.setOnClickListener{
            val intent = Intent(this, RecordingActivity::class.java)
            val sdf = SimpleDateFormat("dd.MM.yyyy") // German date format
            val currentDate = sdf.format(Date())
            intent.putExtra("date", currentDate) // Pass the date to the recording activity to put it as the title
            startActivity(intent)
        }

        //TODO Add history/edit view
        val historyButton = findViewById<Button>(R.id.history_button)
        historyButton.setOnClickListener{
            val intent = Intent(this, HistoryActivity::class.java)
            val sdf = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = sdf.format(Date())
            intent.putExtra("date", currentDate) // Pass the date to the activity so the latest recordings can be shown
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.settings_button)
        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}