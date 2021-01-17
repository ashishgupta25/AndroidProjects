package com.example.deepakk.ola.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.example.deepakk.ola.R;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    RelativeLayout relativeLayout;
    final String TAG = "aaa";
    Switch switch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switch1 = findViewById(R.id.switch1);
        Button btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switch1.isChecked()) {
                    Intent intent = new Intent(MainActivity.this, PassengerMapActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                }
            }

        });
    }

}
