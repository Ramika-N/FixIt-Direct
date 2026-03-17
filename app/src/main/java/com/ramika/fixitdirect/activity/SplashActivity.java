package com.ramika.fixitdirect.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ramika.fixitdirect.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);

                    finish();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
    }
}