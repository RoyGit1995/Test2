package com.example.test2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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


public class ContainerActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final int RECORD_REQUEST_CODE = 101;

    private TextView headingEdit;
    private TextView subjectEdit;
    private TextView nextText;

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setRecordHappened(boolean recordHappened) {
        this.recordHappened = recordHappened;
    }

    private int seconds;
    boolean recordHappened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Intent intent = getIntent();
        String subject = intent.getStringExtra("subject");
        String heading = intent.getStringExtra("heading");
        int secondsSummaryIntent = intent.getIntExtra("secondsSummaryIntent", 0);


        headingEdit = findViewById(R.id.headingText);
        subjectEdit = findViewById(R.id.subjectText);
        nextText = findViewById(R.id.nextText);

        subjectEdit.setText(subject);
        headingEdit.setText(heading);
        nextText.setVisibility(View.VISIBLE);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        tabLayout.addOnTabSelectedListener(this);

// Create a FragmentStateAdapter to populate the ViewPager2 with the Notes and Summary fragments
        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new NotesFragment();
                    case 1:
                        SummaryFragment summaryFragment = new SummaryFragment();
// Pass data to the SummaryFragment using a Bundle
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("recordHappened", recordHappened);
                        bundle.putLong("seconds", seconds);
                        summaryFragment.setArguments(bundle);
                        return summaryFragment;
                    default:
                        throw new IllegalArgumentException("Invalid tab position");
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

// Set the pagerAdapter on the ViewPager2
        viewPager.setAdapter(pagerAdapter);

// Set the ViewPager2 as the TabLayout's ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Notes");
                            break;
                        case 1:
                            tab.setText("Summary");
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid tab position");
                    }
                }
        ).attach();

        viewPager.setUserInputEnabled(false);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);
        if (fragment instanceof SummaryFragment) {
            ((SummaryFragment) fragment).otherTabSelected();
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

}
