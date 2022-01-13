package com.example.whatdidisay

import android.Manifest
import android.content.Context
import android.os.Build
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sun.bob.mcalendarview.MCalendarView
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthChangeListener
import sun.bob.mcalendarview.vo.DateData
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import android.view.ViewGroup

import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import java.io.OutputStreamWriter
import sun.bob.mcalendarview.MarkStyle
import java.io.File
import androidx.core.content.FileProvider
import java.io.FileOutputStream
import androidx.core.app.ActivityCompat
import android.widget.Toast

import android.content.pm.PackageManager

import android.graphics.Canvas
import android.graphics.Paint

import android.graphics.pdf.PdfDocument
import android.util.Log
import java.io.IOException


class HistoryActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    private val STORAGE_PERMISSION_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener{
            super.onBackPressed()
        }

        val calendarView = findViewById<MCalendarView>(R.id.calendar_view)
        calendarView.setOnDateClickListener(object : OnDateClickListener() {
            /**
             * Overwritten onDateClick function of the calendar view
             * Get the selected date and start the rating activity with the data passed to it
             */
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDateClick(view: View?, date: DateData) {
                updateMarks(calendarView, date)
            }

        })

        calendarView.setOnMonthChangeListener(object :
            OnMonthChangeListener() { // Change the text view showing the month's name and the year at the top of the calendar view
            override fun onMonthChange(year: Int, month: Int) {
                val dateText = findViewById<TextView>(R.id.calendar_month_text)
                val monthString =
                    DateFormatSymbols().months[month - 1] // Month -1 because apparently that's just how it works here as well?
                dateText.text =
                    "$monthString - $year" // Set the text view text to show the month's name and year in the shown format
            }
        })

        //TODO Call the edit screen when the edit button is pressed

        //Pass the date and title to the edit activity in the intent (key "date" for date and "title" for the title
        //Date has to have the sdf = SimpleDateFormat("dd.MM.yyyy") // Standard german date format
    }
    private fun callEditScreen(date: String, title: String){
        val sendIntent = Intent(this@HistoryActivity, EditActivity::class.java)
        sendIntent.putExtra("date", date)
        sendIntent.putExtra("title", title)
        startActivity(sendIntent)
    }
    /**
     * Overwritten onResume function called when activity is resumed
     * Reloads all the markings when the history activity is opened
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        unmarkDates()
        val sdf = SimpleDateFormat("dd-MMMM- yyyy") // Month name and year
        val currentDate = sdf.format(Date())
        val dateText = findViewById<TextView>(R.id.calendar_month_text)
        dateText.text = currentDate // Set the date text
        // Load all the entries for the current day when loading the app
        val date = intent.getStringExtra("date")
        if (date != null) {
            buildMeetingPreview(date)
        }

// xt view to show the month's name and year in the format specified previously
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateMarks(calendarView: MCalendarView, date: DateData){

        val calendar = Calendar.getInstance()
        val time = calendar.time
        calendarView.markDate(DateData(time.year, time.month, time.day).setMarkStyle(MarkStyle.BACKGROUND, Color.parseColor("#393E46")))
        calendar.set(
            date.year,
            date.month - 1,
            date.day
        ) // Month -1 because apparently that's just how it works?
        val sdf = SimpleDateFormat("dd.MM.yyyy") // Standard german date format
        val currentDate =
            sdf.format(calendar.timeInMillis) // Get the selected date in the specified format

        clearMeetingPreview()
        buildMeetingPreview(currentDate)
        unmarkDates()
        calendarView.markDate(
            DateData(date.year, date.month, date.day).setMarkStyle(MarkStyle(MarkStyle.BACKGROUND, Color.parseColor(
                "#fd7014"
            ))))
    }
    private fun exportData() {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Person Details")
        sendIntent.type = "text/html"
        startActivity(sendIntent)
    }

    /**
     * Mark all dates that have a rating in the database in the calendar in color
     */
    fun markDates() {

        val calendarView = findViewById<MCalendarView>(R.id.calendar_view)
        val prevMarks =
            calendarView.markedDates.all.toMutableList() // All marked dates in the calendar view before the update

        val iterator =
            prevMarks.iterator() // Use an iterator because the original list must not be altered while it is traversed
        while (iterator.hasNext()) { // Remove all previous markings
            // Markings are removed and not updated because they cannot be overwritten with the new color
            val item = iterator.next()
            calendarView.unMarkDate(item)
            //for (date in dates){ // Mark each day for which a rating exists with the color stated in the database for the given entry
            //  calendarView.markDate(
            //    DateData(date.year, date.month, date.day).setMarkStyle(MarkStyle(MarkStyle.BACKGROUND, Color.parseColor(date.color)))
            // )
            //}
        }
    }
    fun unmarkDates(){
        val calendarView = findViewById<MCalendarView>(R.id.calendar_view)
        val prevMarks =
            calendarView.markedDates.all.toMutableList() // All marked dates in the calendar view before the update
            calendarView.setMarkedStyle(MarkStyle.DOT)
        val iterator =
            prevMarks.iterator() // Use an iterator because the original list must not be altered while it is traversed
        while (iterator.hasNext()) { // Remove all previous markings
            // Markings are removed and not updated because they cannot be overwritten with the new color
            val item = iterator.next()
            calendarView.unMarkDate(item)
    }
        val sdf_year = SimpleDateFormat("yyyy")
        val sdf_month = SimpleDateFormat("MM")
        val sdf_day = SimpleDateFormat("dd")
        val year =  sdf_year.format(Date()).toInt()
        val month =  sdf_month.format(Date()).toInt()
        val day =  sdf_day.format(Date()).toInt()
        calendarView.markDate(DateData(year, month, day).setMarkStyle(MarkStyle.BACKGROUND, Color.parseColor("#393E46")))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createExportDialog(date: String, title: String): AlertDialog.Builder {

            val options = arrayOf(".txt", ".pdf")
            val singleChoiceDialog = AlertDialog.Builder(this)
                .setTitle("Choose file type")
                .setSingleChoiceItems(options, 0) {_, i ->
                    Toast.makeText(this, "You clicked on ${options[i]}", Toast.LENGTH_SHORT).show()}
                            .setPositiveButton(
                                "Export"
                            ) { dialog, id ->
                                exportAsTxt(date, title)
                                if ((dialog as AlertDialog).listView.isItemChecked(0)) {
                                    exportAsTxt(date, title)
                                } else if (dialog.listView.isItemChecked(1)) {
                                    exportAsPdf(date, title)
                                }
                            }
                .setNegativeButton("cancel",
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog
                            })
        singleChoiceDialog.create()
        return singleChoiceDialog
        // Create the AlertDialog object and return it

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun exportAsTxt(date: String, title: String) {
        requestStoragePermission()
        val dbHelper = DatabaseHelper(this)
        val recording_text = dbHelper.getRecording(date, title)?.transcription?.let {String(it)}
        val filename = title + ".txt"
        val filelocation = File(filesDir,"files")
        filelocation.mkdirs()
        val file = File(filelocation, filename)
        val uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        //val targetFilePath = Environment.getExternalStorageDirectory().path + File.separator + "tmp" + File.separator + "test.txt"
        val fOut = FileOutputStream(file)
        val outputStreamWriter = OutputStreamWriter(fOut)
        outputStreamWriter.write(recording_text)
        outputStreamWriter.close()
        //val attachmentUri = Uri.parse(targetFilePath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "vnd.android.cursor.dir/email"
        shareIntent.putExtra(Intent.EXTRA_EMAIL, "sender_mail_id")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        shareIntent.putExtra(Intent.EXTRA_TEXT, ("See the transcription for: $title"))
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent , "Send email..."));
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun exportAsPdf(date: String, title: String) {
        requestStoragePermission()
        val dbHelper = DatabaseHelper(this)
        val recording_text = dbHelper.getRecording(date, title)?.transcription?.let {String(it)}
        val filename = "$title.pdf"
        val filelocation = File(filesDir,"files")
        filelocation.mkdirs()
        //val file = File(filelocation, filename)

        try {
            val file = File(filelocation, filename)
            file.createNewFile()
            val fOut = FileOutputStream(file)
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() //A4 Format
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()
            if (recording_text != null) {
                canvas.drawText(recording_text, 10F, 10F, paint)
            }
            document.finishPage(page)
            document.writeTo(fOut)
            document.close()
            val uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "vnd.android.cursor.dir/email"
            shareIntent.putExtra(Intent.EXTRA_EMAIL, "sender_mail_id")
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
            shareIntent.putExtra(Intent.EXTRA_TEXT, ("See the transcription for: $title"))
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent , "Send email..."));
        }
        catch (e: IOException) {
            Log.i("error", e.getLocalizedMessage())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun buildMeetingPreview(date: String) {
        clearMeetingPreview()
        val dbHelper = DatabaseHelper(this)
        val titles = dbHelper.getTitles(date)
        val scrollView = findViewById<ScrollView>(R.id.scroll_view)
        for (x in titles){
            val record = dbHelper.getRecording(date, x)
            if (record != null) {
                this.createNewRow(record.date, record.title)
            }
        }
        // Set the scroll view to be invisible if no entries have been found
        if (titles.isEmpty()) {
            scrollView.visibility = View.INVISIBLE
        } else {
            // Set the scroll view visibility to be visible
            scrollView.visibility = View.VISIBLE
        }
    }
    private fun clearMeetingPreview() {
        while (null != findViewById<LinearLayout>(R.id.MeetingPreviewRowLayout)) {
            val v = findViewById<LinearLayout>(R.id.MeetingPreviewRowLayout)
            (v.parent as ViewGroup).removeView(v)
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun createNewRow(date: String, title: String) {

        val vi =
            applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = vi.inflate(R.layout.history_edit_row, null)

        val insertPoint = findViewById<View>(R.id.row_layout) as LinearLayout

        insertPoint.addView(
            v,
            0,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        )
        val meetingName = findViewById<TextView>(R.id.meetingName_row)
        meetingName.text = title
        var exportDialogBuilder = createExportDialog(date, title)
        val exportButton = findViewById<Button>(R.id.shareButton)
        exportButton.setOnClickListener {
            exportDialogBuilder.show()
        }
        val editButton = findViewById<Button>(R.id.editButton)
        editButton.setOnClickListener{
            callEditScreen(date, title)
        }
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener{
            deleteMeeting(date, title)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun deleteMeeting(date: String, title: String) {
        val dbHelper = DatabaseHelper(this)
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dbHelper.deleteRecording(date, title)
                        clearMeetingPreview()
                        buildMeetingPreview(date)}
                    DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
                }
            }
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the meeting?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because of this and that")
                .setPositiveButton(
                    "ok"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_CODE
                    )
                }
                .setNegativeButton(
                    "cancel"
                ) { dialog, which -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
