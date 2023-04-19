package com.example.test2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment implements MediaPlayer.OnCompletionListener {

    private static final int RECORD_REQUEST_CODE = 101;

    private Button playButton;
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

    private boolean recordHappenedSummary = false;

    private File[] tracks;

    private Vibrator vibrator;
    private Random random;

    @SuppressWarnings("deprecation")
    Handler handler = new Handler();

    @SuppressWarnings("deprecation")
    Handler handlerRuntime = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);

        playButton = rootView.findViewById(R.id.playButton);
        stopButton = rootView.findViewById(R.id.stopButton);
        backwardButton = rootView.findViewById(R.id.backwardButton);
        forwardButton = rootView.findViewById(R.id.forwardButton);
        nextRecording = rootView.findViewById(R.id.nextButton);
        previousRecording = rootView.findViewById(R.id.previousTr);
        drawingView = rootView.findViewById(R.id.drawing_view);

        // Retrieve data from the arguments Bundle
        Bundle bundle = getArguments();
        recordHappenedSummary = bundle.getBoolean("recordHappened");
        seconds = bundle.getInt("seconds");

        //setting the vibrator
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        random = new Random();

        intializeButtons();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("playButton", "playButton is pressed");
                //This variable detects two types of play, if the play is happening without pause then this is false
                if (playButtonPressed) {
                    //Log.d("playButtonPressed", "inside play button if");
                    playButtonPressed = false;
                    if (recordHappenedSummary) {
                        tracks = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles((dir, name) -> name.endsWith(".mp3"));
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No recording Present", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mediaPlayer = new MediaPlayer();
                    //calling the function to play audio
                    play();

                } else {
                    //Log.d("pauseButtonPressed", "inside pause button");
                    //This variable detects two types of play, if the play is happening after pause then this is false

                    playButtonPressed = true;
                    playButton.setText("Play");
                    pause();
                }
                //this will play the next audio after the current is completed
                mediaPlayer.setOnCompletionListener(SummaryFragment.this);


            }

        });

        //stop button
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMediaPlayer();
            }
        });


        //backward button,sets the current position to -10 seconds
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });

        //backward button,sets the current position to +10 seconds
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        //plays next recording after checking the boundaries
        nextRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pauseButtonPlay) {
                    if (currentTrackIndex + 1 <= tracks.length) {
                        currentTrackIndex = (currentTrackIndex + 1) % tracks.length;
                        play();
                    }
                } else {
                    play();
                }

            }
        });

        //plays previous recording after checking the boundaries, and replays if the current track is the first one
        previousRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((currentTrackIndex - 1 <= tracks.length) && (currentTrackIndex - 1 >= 0)) {
                    if (pauseButtonPlay) {

                        currentTrackIndex = (currentTrackIndex - 1) % tracks.length;
                        play();

                    } else {
                        currentTrackIndex = (currentTrackIndex - 2) % tracks.length;
                        play();
                    }
                } else {
                    currentTrackIndex = -1;
                    play();
                }
            }
        });
        return rootView;
    }


    //stop media player and set the track index to first one as it start from first. -1 due to the currenttrack+1 check in play()
    private void stopMediaPlayer() {
        mediaPlayer.stop();
        currentTrackIndex = -1;
        playButton.setText("Play");
        drawingView.loadFromFile(tracks[tracks.length - 1].getName());
    }

    //oncompletion funtion is set here, in android api 30+, this is the way to use it for more control
    @Override
    public void onCompletion(MediaPlayer mp) {
        // When the current music file finishes playing, play the next one
        Log.d("onCompletion", "onCompletion inside");
        play();
    }

    //play function
    private void play() {
        if (currentTrackIndex <= tracks.length) {
            //if play is happening after pause, so no next track updation, and set the musicposition to the paused value
            if (pauseButtonPlay) {
                Log.d("pauseButtonPlay", "pauseButtonPlay to start from the pause ");
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.seekTo(musicPosition);
                    mediaPlayer.start();
                    //calls the load from file to display the bitmap of the current audio
                    drawingView.loadFromFile(tracks[currentTrackIndex].getName());


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //here its set back as false
                pauseButtonPlay = false;

            } else {
                //boundary check
                if (currentTrackIndex + 1 <= tracks.length) {
                    try {
                        currentTrackIndex = (currentTrackIndex + 1) % tracks.length;
                        Log.d("play currentindex value     ", String.valueOf(currentTrackIndex));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        //set the text back as pause
                        playButton.setText("Pause");
                        //calls the load from file to display the bitmap of the current audio

                        drawingView.loadFromFile(tracks[currentTrackIndex].getName());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.d("play currentindex value     ", String.valueOf(currentTrackIndex));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(tracks[currentTrackIndex].getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        //set the text back as pause
                        playButton.setText("Pause");

                        drawingView.loadFromFile(tracks[currentTrackIndex].getName());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            currentTrackIndex = 0;
        }
    }

    //pause function, also grabs the current position where pause is happened
    private void pause() {
        pauseButtonPlay = true;
        mediaPlayer.pause();
        musicPosition = mediaPlayer.getCurrentPosition();
        Log.d("musicPosition value issssss.....", String.valueOf(musicPosition));
    }



    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
    }

    private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            requestRecordingPermission();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord) {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Given", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void otherTabSelected() {
        pauseButtonPlay = false;
        playButtonPressed = true;
        if (null != mediaPlayer) {
            stopMediaPlayer();
        }
    }

    public void thisTabSelected() {
        recordHappenedSummary = ((ContainerActivity)getActivity()).isRecordHappened();
        seconds = ((ContainerActivity)getActivity()).getSeconds();
        intializeButtons();
    }

    public void intializeButtons()
    {
        if (recordHappenedSummary) {
            playButton.setEnabled(true);
            stopButton.setEnabled(true);
            backwardButton.setEnabled(true);
            forwardButton.setEnabled(true);
            nextRecording.setEnabled(true);
            previousRecording.setEnabled(true);

            tracks = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles((dir, name) -> name.endsWith(".mp3"));
            Log.d("track issss", tracks[tracks.length - 1].getName().toString());
            drawingView.loadFromFile(tracks[tracks.length - 1].getName());

        }
    }
}