package com.example.test2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


//This class is not used, its functionality is moved to Summaryfragment
public class SubjectActivity extends AppCompatActivity {
    private EditText headingEdit;
    private EditText subjectEdit;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        subjectEdit = (EditText) findViewById(R.id.subjectEdit);
        headingEdit = (EditText) findViewById(R.id.headingEdit);
        submitButton = (Button) findViewById(R.id.submitButton);


        //Setting the text as null as the click happens
        subjectEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                subjectEdit.setText("");
                return false;
            }
        });

        //Setting the text as null as the click happens
        headingEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                headingEdit.setText("");
                return false;
            }
        });

        //Setting the text as null as the click happens, if user click around it for good interaction
        subjectEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subjectEdit.setText("");
            }
        });

        //Setting the text as null as the click happens, if user click around it for good interaction
        headingEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headingEdit.setText("");
            }
        });

        //Intent to send data across to notes page
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subjectEditString = "";
                String headingEditString = "";
                subjectEditString = subjectEdit.getText().toString();
                headingEditString = headingEdit.getText().toString();

                Intent intent = new Intent(SubjectActivity.this, ContainerActivity.class);
                intent.putExtra("subject", subjectEditString);
                intent.putExtra("heading", headingEditString);
                startActivity(intent);
            }
        });
    }
}
