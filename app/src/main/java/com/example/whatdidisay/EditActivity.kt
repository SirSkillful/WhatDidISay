package com.example.whatdidisay

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val applyButton = findViewById<Button>(R.id.apply_button)
        applyButton.setOnClickListener {
            //Store the new text in the database as byte stream
            val db = DatabaseHelper(this)
            val date = intent.getStringExtra("date")
            val title = intent.getStringExtra("title")
            val text = findViewById<TextView>(R.id.text_box).text.toString().toByteArray() // Use default charset for encoding
            if (!date.isNullOrEmpty() and !title.isNullOrEmpty()){
                if (db.updateRecording(date.toString(), title.toString(), text)) {// Only update with text
                    //Show that the new text has been stored if there were no errors
                    Toast.makeText(applicationContext, "Update successful", Toast.LENGTH_SHORT).show()
                } else {
                    //Show error message on error
                    Toast.makeText(applicationContext, "There was an error updating the entry", Toast.LENGTH_LONG).show()
                }
            }
        }

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            // Just go back without saving the changes
            super.onBackPressed()
        }

        val imageButton = findViewById<Button>(R.id.add_image_button)
        imageButton.setOnClickListener {
            // Open image finding dialog to get image from gallery
            // Add image to the view
        }
    }

    /**
     * Overwritten onResume function called when activity is resumed
     * Reloads all the markings when the history activity is opened
     */
    override fun onResume() {
        super.onResume()
        //Prepare the window by showing the chosen date and title of the transcript shown
        val textView = findViewById<TextView>(R.id.text_box)
        val transcript_text = findViewById<TextView>(R.id.transcript_text)
        val date = intent.getStringExtra("date")
        val title = intent.getStringExtra("title")
        transcript_text.text = "$date $title" //Format e.g. 01.12.2021 TestMeeting, most likely the title will be the time the meeting was started at
        //Load the text from the database into the edit window
        val db = DatabaseHelper(this)
        val recording = if (!date.isNullOrEmpty() and !title.isNullOrEmpty()) db.getRecording(date.toString(), title.toString()) else null
        // Set the date text view to show the month's name and year in the format specified previously
        recording?.let{ // Only set if the recording is not null
            textView.text = recording.transcription?.let {String(it)}
        }
    }
}