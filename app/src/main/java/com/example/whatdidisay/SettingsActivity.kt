package com.example.whatdidisay

import android.R.attr
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import android.content.Intent
import android.provider.DocumentsContract

import android.R.attr.data
import android.net.Uri
import android.R.attr.data
import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts


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

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data?.data
                // Save the URI for for later saving of
                editor.putString(MainActivity.EXPORT_LOC, intent.toString())
                editor.apply()
                // Show the URI
                val pathView = findViewById<TextView>(R.id.export_path_input)
                pathView.text = intent.toString()
                // Handle the Intent
                // Bottom path is experimental
                //val docUri = DocumentsContract.buildDocumentUriUsingTree(
                //    intent,
                //    DocumentsContract.getTreeDocumentId(intent)
                //)
                //pathView.text = docUri.path
            }
        }

        val expBrowseButton = findViewById<Button>(R.id.export_path_browse_button)
        expBrowseButton.setOnClickListener{
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            //startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999)
            startForResult.launch(i)
            //val uri: Uri = data.getData()
            //val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
            //    uri,
            //    DocumentsContract.getTreeDocumentId(uri)
            //)
            //val path: String = getPath(this, docUri)


        }
        /*
        val audioBrowseButton = findViewById<Button>(R.id.audio_path_browse_button)
        audioBrowseButton.setOnClickListener{
            //TODO Open directory browsing dialog
            //TODO Set input path text to the chosen directory
            //TODO Save setting in shared preferences
        }
        */

        val forwardInput = findViewById<TextView>(R.id.forward_time_input)
        forwardInput.doAfterTextChanged {
            //Save new value in shared preferences
            if (forwardInput.text.isNotEmpty()){
                editor.putInt(MainActivity.FW_TIME, forwardInput.text.toString().toInt())
                Log.d("TEST", forwardInput.text as String)
            } else {
                editor.putInt(MainActivity.FW_TIME, 1)
            }

        }
    }

    override fun onResume(){
        val sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE)
        super.onResume()

        //Load entries from shared preferences
        val forwardInput = findViewById<TextView>(R.id.forward_time_input)
        //forwardInput.text = sharedPrefs.getInt(MainActivity.FW_TIME, 5).toString() //TODO Fix issue translating the value to a string
        val expLocInput = findViewById<TextView>(R.id.export_path_input)
        expLocInput.text = sharedPrefs.getString(MainActivity.EXPORT_LOC, "")
        //val audioLocInput = findViewById<TextView>(R.id.audio_path_input)
        //audioLocInput.text = sharedPrefs.getString(MainActivity.AUDIO_LOC, "")
    }
}