package com.example.whatdidisay

import android.content.Context
import android.os.Build
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import android.os.Environment
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.vo.DayData
import java.io.File


class HistoryActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val calendarView = findViewById<MCalendarView>(R.id.calendar_view)
        calendarView.setOnDateClickListener(object : OnDateClickListener() {
            /**
             * Overwritten onDateClick function of the calendar view
             * Get the selected date and start the rating activity with the data passed to it
             */
            override fun onDateClick(view: View?, date: DateData) {
                val calendar = Calendar.getInstance()
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
                   DateData(date.year, date.month, date.day).setMarkStyle(MarkStyle(MarkStyle.DOT, Color.parseColor(
                       "#fd7014"
                   ))))
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
    fun callEditScreen(date: String, title: String){
        val sendIntent = Intent(this@HistoryActivity, EditActivity::class.java)
        sendIntent.putExtra("date", date)
        sendIntent.putExtra("title", title)
        startActivity(sendIntent)
    }
    /**
     * Overwritten onResume function called when activity is resumed
     * Reloads all the markings when the history activity is opened
     */
    override fun onResume() {
        super.onResume()
        markDates()
        val sdf = SimpleDateFormat("dd-MMMM- yyyy") // Month name and year
        val currentDate = sdf.format(Date())
        val dateText = findViewById<TextView>(R.id.calendar_month_text)
        dateText.text =
            currentDate // Set the date text view to show the month's name and year in the format specified previously
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
        val dbHelper = DatabaseHelper(this) // DB Communicator
        //val dates = dbHelper.getRecording(currentDate) // Get all ratings in the database
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
    }

    private fun createExportDialog(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose file type")
            .setPositiveButton("Export", DialogInterface.OnClickListener { dialog, id ->
                exportAsTxt()
            })
            //.setNeutralButton("Export as .pdf", DialogInterface.OnClickListener { dialog, id ->
             //   exportData()
            //})
            .setNegativeButton("cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
            })
            val options = arrayOf(".txt", ".pdf")
            val singleChoiceDialog = AlertDialog.Builder(this)
                .setTitle("Choose file type")
                .setSingleChoiceItems(options, 0) {dialogInterface, i ->
                    Toast.makeText(this, "You clicked on ${options[i]}", Toast.LENGTH_SHORT).show()}
                            .setPositiveButton("Export", DialogInterface.OnClickListener { dialog, id ->
                                exportAsTxt()
                            })
                .setNegativeButton("cancel",
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog
                            })
        singleChoiceDialog.create()
        return singleChoiceDialog
        // Create the AlertDialog object and return it

    }

    private fun exportAsTxt() {
        val targetFilePath = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + "tmp" + File.separator + "test.txt"
        val outputStreamWriter =
            OutputStreamWriter(openFileOutput(targetFilePath, MODE_PRIVATE))
        outputStreamWriter.write("test")
        outputStreamWriter.close()
        val attachmentUri = Uri.parse(targetFilePath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "message/rfc822"
        shareIntent.putExtra(Intent.EXTRA_EMAIL, "sender_mail_id")
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Meeting Name")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Text to be displayed as the content")
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$attachmentUri"))
        startActivity(shareIntent)
    }

    fun buildMeetingPreview(date: String) {
        val dbHelper = DatabaseHelper(this)
        val arr = ByteArray(8)
        //dbHelper.addRecording(date.toString(), date.toString(), arr)
        val titles = dbHelper.getTitles(date)
        for (x in titles){
            val record = dbHelper.getRecording(date, x)
            if (record != null) {
                this.createNewRow(record.date, record.title)
            }
        }
    }
    fun clearMeetingPreview() {
        while (null != findViewById<LinearLayout>(R.id.MeetingPreviewRowLayout)) {
            val v = findViewById<LinearLayout>(R.id.MeetingPreviewRowLayout)
            (v.parent as ViewGroup).removeView(v)
        }
    }
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
        var exportDialogBuilder = createExportDialog()
        val exportButton = findViewById<Button>(R.id.shareButton)
        exportButton.setOnClickListener {
            exportDialogBuilder.show()
        }
        val editButton = findViewById<Button>(R.id.editButton)
        editButton.setOnClickListener{
            callEditScreen(date, title)
        }
    }
}
