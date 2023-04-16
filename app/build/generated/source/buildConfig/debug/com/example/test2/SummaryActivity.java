package com.example.test2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SummaryActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

private static final int RECORD_REQUEST_CODE = 101;

private Button playButton;
private TextView viewById;
private Button stopButton;
private Button backwardButton;
private Button forwardButton;
private DrawingView drawingView;
private Button nextRecording;
private Button previousRecording;
private Button noteButton;
private Button summaryButton;


private MediaRecorder mediaRecorder;
private MediaPlayer mediaPlayer;

private String fileName;

private File file;
private File fileRecord;

private boolean isRecording = false;

private boolean isPlayingMedia = false;

private boolean playButtonPressed = true;

private int musicPosition = 0;

private boolean pauseButtonPlay = false;

        ExecutorService executorService = Executors.newSingleThreadExecutor();

private int playableSeconds, seconds, dummySeconds = 0;

private int currentTrackIndex = -1;

private boolean recordHappenedSummary, recordHappened = false;

private File[] tracks;

private Vibrator vibrator;
private Random random;

@SuppressWarnings("deprecation")
    Handler handler = new Handler();

@SuppressWarnings("deprecation")
    Handler handlerRuntime = new Handler();

        private TextView headingEdit;
        private TextView subjectEdit;
        private TextView nextText;


@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summaryscreen);

        playButton = (Button) findViewById(R.id.playButton);
        viewById = (TextView) findViewById(R.id.runTime);
        stopButton = (Button) findViewById(R.id.stopButton);
        backwardButton = (Button) findViewById(R.id.backwardButton);
        forwardButton = (Button) findViewById(R.id.forwardButton);
        nextRecording = (Button) findViewById(R.id.nextButton);
        previousRecording = (Button) findViewById(R.id.previousTr);
        mediaPlayer = new MediaPlayer();

        headingEdit = findViewById(R.id.headingText);
        subjectEdit = findViewById(R.id.subjectText);
        nextText = findViewById(R.id.nextText);

        Intent intent = getIntent();
        String subject = intent.getStringExtra("subject");
        String heading = intent.getStringExtra("heading");
        recordHappenedSummary = intent.getBooleanExtra("recordHappened",recordHappened);
        int secondsSummaryIntent = intent.getIntExtra("seconds",seconds);


        subjectEdit.setText(subject);
        headingEdit.setText(heading);
        nextText.setVisibility(View.VISIBLE);

        drawingView = findViewById(R.id.drawing_view);

        tracks = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles((dir, name) -> name.endsWith(".mp3"));
        Log.d("track issss", tracks[tracks.length-1].getName().toString());
        drawingView.loadFromFile(tracks[tracks.length-1].getName());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        random = new Random();

        noteButton = (Button) findViewById(R.id.noteButton);
        summaryButton = (Button) findViewById(R.id.summaryButton);

        summaryButton.setEnabled(false);
        noteButton.setEnabled(true);

        if (recordHappenedSummary) {
                playButton.setEnabled(true);
                stopButton.setEnabled(true);
                backwardButton.setEnabled(true);
                forwardButton.setEnabled(true);
                nextRecording.setEnabled(true);
                previousRecording.setEnabled(true);

        }

        noteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        String subjectEditString = "";
                        String headingEditString = "";
                        subjectEditString = subjectEdit.getText().toString();
                        headingEditString = headingEdit.getText().toString();


                        Intent intent = new Intent(SummaryActivity.this, FinalActivity.class);
                        intent.putExtra("subject", subjectEditString);
                        intent.putExtra("heading", headingEditString);
                        intent.putExtra("secondsSummaryIntent", secondsSummaryIntent);
                        startActivity(intent);
                        if(null != mediaRecorder)
                        {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                        }
                }
        });



        playButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        //Log.d("playButton", "playButton is pressed");

        if (playButtonPressed)
        {
        //Log.d("playButtonPressed", "inside play button if");
        playButtonPressed = false;
        if (recordHappenedSummary) {
        tracks = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles((dir, name) -> name.endsWith(".mp3"));
        } else {
        Toast.makeText(getApplicationContext(), "No recording Present", Toast.LENGTH_SHORT).show();
        return;
        }
        mediaPlayer = new MediaPlayer();
        play();

        }
        else
        {
        //Log.d("pauseButtonPressed", "inside pause button");
        playButtonPressed = true;
        playButton.setText("Play");
        pause();
        }
        mediaPlayer.setOnCompletionListener(SummaryActivity.this);


        }

        });

        stopButton.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        mediaPlayer.stop();
        currentTrackIndex = -1;
        playButton.setText("Play");
        drawingView.loadFromFile(tracks[tracks.length].getName());
        }
        });

        backwardButton.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        if(mediaPlayer != null){
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
        }
        }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        if(mediaPlayer != null){
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
        }
        }
        });

        nextRecording.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {

        if (pauseButtonPlay) {
        if (currentTrackIndex + 1 <= tracks.length) {
        currentTrackIndex = (currentTrackIndex + 1) % tracks.length;
        play();
        }
        }
        else
        {
        play();
        }

        }
        });


        previousRecording.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        if ((currentTrackIndex - 1 <= tracks.length) && (currentTrackIndex - 1 >= 0)) {
        if (pauseButtonPlay) {

        currentTrackIndex = (currentTrackIndex - 1) % tracks.length;
        play();

        }
        else {
        currentTrackIndex = (currentTrackIndex - 2) % tracks.length;
        play();
        }
        }
        else
        {
        currentTrackIndex = -1;
        play();
        }
        }
        });

        }





@Override
public void onCompletion(MediaPlayer mp) {
        // When the current music file finishes playing, play the next one
        Log.d("onCompletion", "onCompletion inside");
        play();
        }

private void play() {
        if (currentTrackIndex <= tracks.length) {
        if (pauseButtonPlay) {
        Log.d("pauseButtonPlay", "pauseButtonPlay to start from the pause ");
        mediaPlayer.reset();
        try {
        mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
        mediaPlayer.prepare();
        mediaPlayer.seekTo(musicPosition);
        mediaPlayer.start();

        drawingView.loadFromFile(tracks[currentTrackIndex].getName());


        } catch (IOException e) {
        throw new RuntimeException(e);
        }
        pauseButtonPlay = false;

        } else {
        if (currentTrackIndex + 1 <= tracks.length) {
        try {
        currentTrackIndex = (currentTrackIndex + 1) % tracks.length;
        Log.d("play currentindex value     ", String.valueOf(currentTrackIndex));
        mediaPlayer.reset();
        mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
        mediaPlayer.prepare();
        mediaPlayer.start();
        playButton.setText("Pause");

        drawingView.loadFromFile(tracks[currentTrackIndex].getName());

        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        else {
        try {
        Log.d("play currentindex value     ", String.valueOf(currentTrackIndex));
        mediaPlayer.reset();
        mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
        mediaPlayer.prepare();
        mediaPlayer.start();
        playButton.setText("Pause");

        drawingView.loadFromFile(tracks[currentTrackIndex].getName());

        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        }
        }
        else {
        currentTrackIndex = 0;
        }
        }

private void pause() {
        pauseButtonPlay = true;
        mediaPlayer.pause();
        musicPosition = mediaPlayer.getCurrentPosition();
        Log.d("musicPosition value issssss.....", String.valueOf(musicPosition));
        }

private void saveAudioFile() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

        try {
        OutputStream outputStream = contentResolver.openOutputStream(uri);
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        outputStream.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }


private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(SummaryActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
        }

private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
        {
        requestRecordingPermission();
        return false;
        }
        return true;
        }

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==RECORD_REQUEST_CODE)
        {
        if(grantResults.length>0)
        {
        boolean permissionToRecord = grantResults[0]==PackageManager.PERMISSION_GRANTED;
        if(permissionToRecord)
        {
        Toast.makeText(getApplicationContext(), "Permission Given",Toast.LENGTH_SHORT).show();
        }
        else
        {
        Toast.makeText(getApplicationContext(), "Permission Denied",Toast.LENGTH_SHORT).show();
        }
        }
        }
        }
private File getRecordingFilePath() {

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Audio Recordings");

        Log.d("Directory", directory.getAbsolutePath().toString());

        if (!directory.exists()) {
        directory.mkdirs();
        }
        else {
        File[] files = directory.listFiles();
        if(files != null) {
        for(File file : files) {
        Log.d("Files", file.getName().toString() + "         "  +  file.getAbsolutePath());
        file.delete();
        }
        }
        }
        return directory;
        }



        }
