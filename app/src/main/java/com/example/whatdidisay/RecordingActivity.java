package com.example.whatdidisay;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class RecordingActivity extends AppCompatActivity {

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private EditText meetingTitle;
    private TextView meetingDate;
    private EditText displayText;
    private Button recordButton;
    private Button backButton;
    private Button backwardRecordButton;
    private Button forwardRecordButton;
    private String entireText = ""; // The entire meeting
    private String partialText = ""; // The part of the meeting starting from the click of the forward button
    private boolean isRecordingActive = false; // false:= recording has not started, true := currently recording
    private boolean isForwardRecordingActive = false; // false:= forward recording has not started, true := currently recording
    private AlertDialog.Builder builder;
    private static int seconds = 10; // ToDo: get the number of seconds from settings
    private static final double NUMBER_OF_WORDS_PER_SECOND = 120.0 / 60.0; // Assumed a person says on an average 120 words in a minute.
    private String title = "default title";
    private Location location = null;

    /**
     * @return the number of words that a person
     * on an average says during the given time in seconds
     * */
    private static int secondsToNumberOfWords(int seconds) {
        return (int) (seconds * NUMBER_OF_WORDS_PER_SECOND);
    }

    /**
     * @param text The spoken text by the user
     * @param numberOfWords The number of the x last words to extract from the text
     * @return the last X words of a given text
     * */
    private static String getLastXWords(String text, int numberOfWords) {
        String result = "";

        String[] words = text.split(" ");

        if (words.length <= numberOfWords) return text;

        for (int i = words.length - numberOfWords; i < words.length; i++) {
            result += words[i];
            if (i < words.length - 1) { // Add space after word if it is not the last word
                result += " ";
            }
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_activity);

        checkAudioInputPermission(); // Checking Permission on Runtime. Important for Android Marshmallow or later

        meetingTitle = findViewById(R.id.meeting_title);
        meetingDate = findViewById(R.id.transcript_text);
        displayText = findViewById(R.id.text_box);
        recordButton = (Button) findViewById(R.id.record_button);
        backButton = (Button) findViewById(R.id.back_button);
        backwardRecordButton = (Button) findViewById(R.id.backward_record_button);
        forwardRecordButton = (Button) findViewById(R.id.forward_record_button);
        DatabaseHelper db = new DatabaseHelper(this);

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);

        meetingTitle.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // The title of the meeting is updated when the user changes the default title
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                title = String.valueOf(meetingTitle.getText());
            }
        });

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String part = (String) matches.get(matches.size() - 1);
                entireText += part + " ";
                if (isForwardRecordingActive) {
                    partialText += part + " ";
                    displayText.setText(partialText);
                }
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }


            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String part = (String) matches.get(matches.size() - 1);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }


            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecordingActive) { // recording is active
                    builder.setMessage("End recording?");
                    builder.setPositiveButton(
                            "Save",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mSpeechRecognizer.stopListening();
                                    // ToDo write text (the value of the entireText variable) in the database
                                    String title = meetingTitle.getText().toString();
                                    String date = meetingDate.getText().toString();
                                    byte[] transcript_text = entireText.getBytes();
                                    db.addRecording(date, title, transcript_text, null);
                                    Recording recording_example = db.getRecording(date, title);
                                    //Toast.makeText(RecordingActivity, recording_example.getTranscription(), Toast.LENGTH_SHORT).show();
                                    //Log.i("entire text:", entireText);
                                    displayText.setText("");
                                    displayText.setHint("Finished recording and ready to start a new one.");
                                    isRecordingActive = false;
                                }
                            });

                    builder.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else { // recording is not active
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    displayText.setHint("Listening...");
                    isRecordingActive = true;
                    // Get current location
                    logLocation();
                    // ToDo: if location != null add it to the recording
                    // ToDo: Display location in Analysis Activity
                }
            }
        });

        forwardRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isForwardRecordingActive) { // recording is active
                    isForwardRecordingActive = false;
                } else { // recording is not active
                    displayText.setHint("Listening...");
                    isForwardRecordingActive = true;
                }
            }
        });

        backwardRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayText.setText(getLastXWords(entireText, secondsToNumberOfWords(seconds)));
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecordingActive) {
                    builder.setMessage("End recording without saving?");
                    builder.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                    builder.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    finish();
                }
            }
        });

    }

    private void checkAudioInputPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //Set the date label
        String currentDate = getIntent().getStringExtra("date");
        TextView dateText = (TextView) findViewById(R.id.transcript_text);
        dateText.setText(currentDate);
    }

    public void logLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                location = lastKnownLocationGPS;
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);;
            }
        } else {
            Toast.makeText(this, "Location Services Not Working", Toast.LENGTH_SHORT).show();
        }
//        System.out.println("Longitude: " + location.getLongitude());
//        System.out.println("Latitude: " + location.getLatitude());
    }

}
