package com.example.test2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private static final int RECORD_REQUEST_CODE = 101;

    private Button playButton;
    private Button recordButton;
    private TextView viewById;
    private Button stopButton;
    private Button backwardButton;
    private Button forwardButton;

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
        stopButton = (Button) findViewById(R.id.stopButton);
        backwardButton = (Button) findViewById(R.id.backwardButton);
        forwardButton = (Button) findViewById(R.id.forwardButton);
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
                                        playButton.setEnabled(false);
                                        stopButton.setEnabled(false);
                                        backwardButton.setEnabled(false);
                                        //playableSeconds = 0;
                                        //seconds = 0;
                                        //dummySeconds = 0;

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
                                //seconds = 0;
                                isRecording = false;



                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#ffffff"));
                                        playButton.setEnabled(true);
                                        stopButton.setEnabled(true);
                                        backwardButton.setEnabled(true);

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


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("playButton", "playButton is pressed");

                if (playButtonPressed)
                {
                    //Log.d("playButtonPressed", "inside play button if");
                    playButtonPressed = false;
                    if (fileName != null) {
                        tracks = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles((dir, name) -> name.endsWith(".mp3"));

                    } else {
                        Toast.makeText(getApplicationContext(), "No residing Present", Toast.LENGTH_SHORT).show();
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
                mediaPlayer.setOnCompletionListener(MainActivity.this);
            }

        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                currentTrackIndex = -1;
                playButton.setText("Play");
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                pauseButtonPlay = false;

            } else {

                try {
                    currentTrackIndex = (currentTrackIndex + 1) % tracks.length;
                    Log.d("play", "inside play function");
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playButton.setText("Pause");

                } catch (IOException e) {
                    e.printStackTrace();
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



