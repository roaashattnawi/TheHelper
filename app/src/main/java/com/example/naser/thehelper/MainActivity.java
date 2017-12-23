package com.example.naser.thehelper;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                doStuff();
            }
        }, 3000);
    }

    private void doStuff() {
        Intent intent = new Intent(MainActivity.this, MaidOrCustomerActivtiy.class);
        startActivity(intent);
    }
}

