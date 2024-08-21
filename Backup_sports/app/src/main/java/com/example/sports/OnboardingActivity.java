package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

    loginButton.setOnClickListener(v ->
               startActivity(new Intent(OnboardingActivity.this, LoginActivity.class)));
    signupButton.setOnClickListener(v ->
              startActivity(new Intent(OnboardingActivity.this, RegisterActivity.class)));

    }
}

