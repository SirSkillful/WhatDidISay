package com.example.whatdidisay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RecordingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val forwardButton = findViewById<Button>(R.id.add_image_button)
        forwardButton.setOnClickListener {
            //TODO Transcribe next x seconds
        }
        //TODO either open the dialog after holding the button via using setOnTouchListere (complicated) or just stick with the settings.

        val backtrackButton = findViewById<Button>(R.id.apply_button)
        backtrackButton.setOnClickListener {
            //TODO Transcribe last x seconds
        }
        //TODO either open the dialog after holding the button via using setOnTouchListere (complicated) or just stick with the settings.
    }
    
    override fun onResume() {
        super.onResume()
        //Set the date label
        val currentDate = intent.getStringExtra("date")
        val dateText = findViewById<TextView>(R.id.transcript_text)
        dateText.text = currentDate
    }
}