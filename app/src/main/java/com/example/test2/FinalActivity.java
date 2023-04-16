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
import android.widget.EditText;
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


public class FinalActivity extends AppCompatActivity {

    private static final int RECORD_REQUEST_CODE = 101;

    private Button playButton;
    private Button recordButton;
    private TextView viewById;
    private Button stopButton;
    private Button backwardButton;
    private Button forwardButton;
    private DrawingView drawingView;
    private Button nextRecording;
    private Button previousRecording;
    private Switch vibrateSwitch;
    private Button summaryButton;
    private Button notesButton;



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
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String subject = intent.getStringExtra("subject");
        String heading = intent.getStringExtra("heading");


        recordButton = (Button) findViewById(R.id.recordButton);
        viewById = (TextView) findViewById(R.id.runTime);
        vibrateSwitch = findViewById(R.id.vibrate_switch);
        mediaPlayer = new MediaPlayer();

        drawingView = findViewById(R.id.drawing_view);

        headingEdit = findViewById(R.id.headingText);
        subjectEdit = findViewById(R.id.subjectText);
        nextText = findViewById(R.id.nextText);

        subjectEdit.setText(subject);
        headingEdit.setText(heading);
        nextText.setVisibility(View.VISIBLE);

        summaryButton = (Button) findViewById(R.id.summaryButton);
        notesButton = (Button) findViewById(R.id.noteButton);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        random = new Random();

        notesButton.setEnabled(false);
        summaryButton.setEnabled(true);

        //recording

        summaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subjectEditString = "";
                String headingEditString = "";
                subjectEditString = subjectEdit.getText().toString();
                headingEditString = headingEdit.getText().toString();

                Intent intent = new Intent(FinalActivity.this, SummaryActivity.class);
                intent.putExtra("subject", subjectEditString);
                intent.putExtra("heading", headingEditString);
                startActivity(intent);
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkRecordingPermission())
                {
                    if(!isRecording)
                    {
                        isRecording = true;
                        fileRecord = getRecordingFilePath();

                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        fileName = "AUDIO_" + timeStamp + ".mp3";
                        file = new File(fileRecord, fileName);

                        //check if its
                        // the first time and then dont make directory
                        executorService.execute(new Runnable() {

                            @Override
                            public void run() {
                                mediaRecorder = new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                                mediaRecorder.setOutputFile(file.getAbsolutePath());

                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                                mediaRecorder.setAudioEncodingBitRate(128000);
                                mediaRecorder.setAudioSamplingRate(44100);

                                try {
                                    mediaRecorder.prepare();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                mediaRecorder.start();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#FF0000"));
                                        runTimer();

                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                saveAudioFile();
                                mediaRecorder = null;
                                playableSeconds = seconds;
                                dummySeconds = seconds;
                                isRecording = false;

                                drawingView.saveToFile(fileName);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#ffffff"));
                                        handler.removeCallbacksAndMessages(null);


                                    }
                                });


                            }
                        });
                    }
                }

                else
                {
                    requestRecordingPermission();
                }
            }
        });



        vibrateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vibrateSwitch.isChecked()) {
                    startVibration();
                }
                else {
                    stopVibration();

                }
            }
        });

    }

    private void startVibration() {
        long[] pattern = generateRandomPattern();
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));

    }

    private void stopVibration() {
        vibrator.cancel();
    }

    private long[] generateRandomPattern() {
        Random random = new Random();
        int size = random.nextInt(5) + 5; // Generate a pattern of 5 to 10 elements
        long[] pattern = new long[size];
        for (int i = 0; i < size; i++) {
            pattern[i] = random.nextInt(500) + 500; // Generate random duration between 500ms to 1000ms
        }
        return pattern;
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

    private void runTimer()
    {
        handlerRuntime.post(new Runnable() {
            @Override
            public void run() {
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;
                String time = String.format(Locale.getDefault(),"%02d:%02d",minutes,secs);
                viewById.setText(time);

                if(isRecording || (isPlayingMedia && playableSeconds!= -1))
                {
                    seconds++;
                    playableSeconds--;

                    if(playableSeconds == -1 && isPlayingMedia)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        mediaPlayer = new MediaPlayer();
                        playableSeconds = dummySeconds;
                        seconds = 0;
                        handler.removeCallbacksAndMessages(null);

                        recordButton.setTextColor(Color.parseColor("#FF0000"));
                        return;

                    }
                }

                handler.postDelayed(this,1000);
            }
        });

    }

    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(FinalActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
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
