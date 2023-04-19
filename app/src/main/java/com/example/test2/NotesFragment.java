package com.example.test2;

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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

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

    boolean recordHappened =false;

    public NotesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recordButton = view.findViewById(R.id.recordButton);
        viewById = view.findViewById(R.id.runTimeText);
        vibrateSwitch = view.findViewById(R.id.vibrate_switch);
        mediaPlayer = new MediaPlayer();

        drawingView = view.findViewById(R.id.drawing_view);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        random = new Random();


        //recording on click listener
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkRecordingPermission())
                {
                    if(!isRecording)
                    {
                        //To know whether its recording or not
                        isRecording = true;
                        //Its a one time true setter for enabling the play,stop,forward,backward buttons in summary page
                        recordHappened = true;
                        ((ContainerActivity)getActivity()).setRecordHappened(recordHappened);

                        fileRecord = getRecordingFilePath();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        fileName = "AUDIO_" + timeStamp + ".mp3";
                        file = new File(fileRecord, fileName);

                        //check if its
                        // the first time and then dont make directory
                        executorService.execute(new Runnable() {

                            @Override
                            public void run() {
                                //Loading the mediarecorder
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

                                //UI changes on another thread
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#FF0000"));
                                        viewById.setEnabled(true);
                                        runTimer();

                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        //If recording is stopped
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                //To save audio file, after the stop button is pressed.
                                saveAudioFile();
                                mediaRecorder = null;
                                playableSeconds = seconds;
                                dummySeconds = seconds;
                                isRecording = false;

                                drawingView.saveToFile(fileName);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordButton.setTextColor(Color.parseColor("#ffffff"));
                                        handler.removeCallbacksAndMessages(null);
                                        viewById.setEnabled(false);

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
                //((ContainerActivity)getActivity()).setRecordHappened(recordHappened);
                ((ContainerActivity)getActivity()).setSeconds(seconds);
            }
        });


        //Vibrate switch
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
        return view;
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


    //To save audio file
    private void saveAudioFile() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);

        ContentResolver contentResolver = getActivity().getContentResolver();
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

    //Runtimer takes the universal seconds variable and keep adding it, it uses postdelay to add 1 second each
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

    //requesting permission for storage, corresponding code in manifest
    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
    }

    private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
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
                    Toast.makeText(requireContext(), "Permission Given",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(requireContext(), "Permission Denied",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Filepath is loaded
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
