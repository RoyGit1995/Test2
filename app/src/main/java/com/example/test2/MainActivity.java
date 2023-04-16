package com.example.test2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText headingEdit;
    private EditText subjectEdit;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detaillayout);

        subjectEdit = (EditText) findViewById(R.id.subjectEdit);
        headingEdit = (EditText) findViewById(R.id.headingEdit);
        submitButton = (Button) findViewById(R.id.submitButton);



        subjectEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                subjectEdit.setText("");
                return false;
            }
        });

        headingEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                headingEdit.setText("");
                return false;
            }
        });


        subjectEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subjectEdit.setText("");
            }
        });

        headingEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headingEdit.setText("");
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subjectEditString = "";
                String headingEditString = "";
                subjectEditString = subjectEdit.getText().toString();
                headingEditString = headingEdit.getText().toString();

                Intent intent = new Intent(MainActivity.this, FinalActivity.class);
                intent.putExtra("subject", subjectEditString);
                intent.putExtra("heading", headingEditString);
                startActivity(intent);
            }
        });
    }
}
