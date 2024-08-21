package com.example.sports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Check if the user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Intent intent;

        if (mAuth.getCurrentUser() != null) {
            // User is logged in, redirect to MainActivity
            intent = new Intent(SplashscreenActivity.this, MainActivity.class);
        } else {
            // User is not logged in, redirect to OnboardingActivity
            intent = new Intent(SplashscreenActivity.this, OnboardingActivity.class);
        }

        // Delay for 3 seconds before starting the next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 3000); // 3 seconds delay
    }
}
