package com.example.whatdidisay

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val RECORDING_TABLE_NAME = "recordings"
private const val DATE_COLUMN = "date"
private const val TITLE_COLUMN = "title"
private const val TRANSCRIPTION_COLUMN = "transcription"
private const val AUDIO_COLUMN = "audio"


private const val CREATE_TABLE =
    "CREATE TABLE $RECORDING_TABLE_NAME ($DATE_COLUMN TEXT," +
            " $TITLE_COLUMN TEXT," +
            " $TRANSCRIPTION_COLUMN  BLOB," +
            " $AUDIO_COLUMN BLOB)"

private const val DELETE_RECORDINGS_TABLE =
    "DROP TABLE IF EXISTS $RECORDING_TABLE_NAME"

class DatabaseHelper (context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    var db = this.writableDatabase // DB connection instance

    // Public static variables
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "WDIS.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create the table when installing the app
        db?.execSQL(CREATE_TABLE)
        this.db = db
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DELETE_RECORDINGS_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    /**
     * Add a recording to the database.
     * Audio recording is optional
     */
    private fun addRecording(date: String, title: String, transcription: ByteArray, audio: ByteArray? = null): Boolean {
        val values = ContentValues().apply{
            put(DATE_COLUMN, date)
            put(TITLE_COLUMN, title)
            put(TRANSCRIPTION_COLUMN, transcription)
            audio?.let{
                put(AUDIO_COLUMN, audio)
            }
        }
        val ret = db?.insert(RECORDING_TABLE_NAME, null, values)
        return ret?.toInt() != -1 // Return -1 if an error occured during insert
    }

    /**
     * Update the entries of a specified recording.
     * Either update the transcription, the sound file or both
     */
    private fun updateRecording(date: String, title: String, transcription: ByteArray? = null, audio: ByteArray? = null): Boolean{
        val values = ContentValues().apply{
            transcription?.let{
                put(TRANSCRIPTION_COLUMN, transcription)
            }
            audio?.let{
                put(AUDIO_COLUMN, audio)
            }
        }
        // Affected row
        val selection = "$DATE_COLUMN LIKE ? AND $TITLE_COLUMN LIKE ?"
        val selectionArgs = arrayOf(date, title)
        //Update the selected row
        val count = db.update(RECORDING_TABLE_NAME, values, selection, selectionArgs)
        return count > 0 // Return wether rows have been affected
    }

    /**
     * Get the data of a specific recording from the database in form a of a recording object
     */
    private fun getRecording(date: String, title: String): Recording?{
        val projection = arrayOf(DATE_COLUMN, TITLE_COLUMN, TRANSCRIPTION_COLUMN, AUDIO_COLUMN)
        val selection = "$DATE_COLUMN LIKE ? AND $TITLE_COLUMN LIKE ?"
        val selectionArgs = arrayOf(date, title)
        val cursor = db.query(
            RECORDING_TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var recording:Recording? = null
        with(cursor){
            if (moveToNext()) {
                val date = getString(getColumnIndexOrThrow(DATE_COLUMN))
                val title = getString(getColumnIndexOrThrow(TITLE_COLUMN))
                val transcription = getBlob(getColumnIndexOrThrow(TRANSCRIPTION_COLUMN))
                val audio = getBlob(getColumnIndex(AUDIO_COLUMN))
                recording = Recording(date, title, transcription, audio)
            }
        }
        cursor.close()
        return recording
    }

    /**
     * Delete a recording and associated text
     */
    private fun deleteRecording(date: String, title: String): Boolean{
        //Select the recording
        val selection = "$DATE_COLUMN LIKE ? AND $TITLE_COLUMN LIKE ?"
        val selectionArgs = arrayOf(date, title)
        //Delete it
        val deletedRows = db.delete(RECORDING_TABLE_NAME, selection, selectionArgs)
        //Return wether any rows have been affected
        return deletedRows > 0
    }
}