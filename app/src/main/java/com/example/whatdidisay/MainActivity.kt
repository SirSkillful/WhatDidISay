package com.example.whatdidisay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recordingButton = findViewById<Button>(R.id.record_button)
        recordingButton.setOnClickListener{
            val intent = Intent(this, RecordingActivity::class.java)
            val sdf = SimpleDateFormat("dd.MM.yyyy") // German date format
            val currentDate = sdf.format(Date())
            intent.putExtra("date", currentDate) // Pass the date to the recording activity to put it as the title
            startActivity(intent)
        }

        //TODO Add history/edit view

        //TODO Add settings menu
    }
}