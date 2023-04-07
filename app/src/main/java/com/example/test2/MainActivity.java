package com.example.test2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_REQUEST_CODE = 101;

    private Button playButton;
    private Button recordButton;
    private TextView viewById;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private String filePath;

    private boolean isRecording = false;

    private boolean isPlaying = false;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int playableSeconds, seconds, dummySeconds = 0;

    @SuppressWarnings("deprecation")
    Handler handler = new Handler();

    @SuppressWarnings("deprecation")
    Handler handlerRuntime = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button) findViewById(R.id.playButton);
        viewById = (TextView) findViewById(R.id.runTime);
        mediaPlayer = new MediaPlayer();


        //recording
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkRecordingPermission())
                {
                    if(!isRecording)
                    {
                        isRecording = true;
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder = new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                mediaRecorder.setOutputFile(getRecordingFilePath());
                                filePath = getRecordingFilePath();
                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

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

                                        playableSeconds = 0;
                                        seconds = 0;
                                        dummySeconds = 0;

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
                                mediaRecorder = null;
                                playableSeconds = seconds;
                                dummySeconds = seconds;
                                seconds = 0;
                                isRecording = false;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#ffffff"));
                                        playButton.setEnabled(true);
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

        //play
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isPlaying)
                {
                    if(filePath!=null)
                    {
                        try {
                            mediaPlayer.setDataSource(getRecordingFilePath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No residing Present",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    mediaPlayer.start();
                    isPlaying = true;
                    recordButton.setTextColor(Color.parseColor("#FF0000"));

                    playButton.setEnabled(false);
                    runTimer();
                }
                else
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    mediaPlayer = new MediaPlayer();
                    isPlaying = false;
                    seconds = 0;

                    handler.removeCallbacksAndMessages(null);
                }

            }

        });

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

                if(isRecording || (isPlaying && playableSeconds!= -1))
                {
                    seconds++;
                    playableSeconds--;

                    if(playableSeconds == -1 && isPlaying)
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
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
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
    private String getRecordingFilePath()
    {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music, "AudioFile.3gp");
        Log.d(filePath,"The file path isssssssss" + file.getPath().toString());

        //File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/MyApp/Audio");
        //File fileEx = new File(directory, "AudioFileEx"+".mp3");
        //Log.d(filePath,fileEx.getPath().toString());




            try {
                // Open a file output stream

                FileOutputStream outputStream = new FileOutputStream(file);
                Log.d(filePath,"FileOutputStream outputStream");

                // Copy the MP3 file from your app's assets folder to the file output stream

                InputStream inputStream = getAssets().open("AudioFile.3gp");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                Log.d(filePath,"InputStream inputStream");

                // Close the streams
                inputStream.close();
                outputStream.flush();
                outputStream.close();

                // Print a success message
                System.out.println("File saved successfully!");
            } catch (IOException e) {
                // Print an error message
                System.err.println("Error saving file: " + e.getMessage());
            }
            return file.getPath();






    }
}



