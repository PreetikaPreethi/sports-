package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageView backButton;
    private Button loginButton;
    private TextView registerNowTextView;
    private ImageView togglePasswordVisibility;
    private boolean isPasswordVisible = false;
    private TextView forgotPasswordText;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.email); // Changed to email for consistency
        passwordEditText = findViewById(R.id.password);
        backButton = findViewById(R.id.back_arrow);
        loginButton = findViewById(R.id.login_button);
        registerNowTextView = findViewById(R.id.register_now);
        togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Set click listeners
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());

        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            } else {
                passwordEditText.setTransformationMethod(new android.text.method.HideReturnsTransformationMethod());
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_on);
            }
            isPasswordVisible = !isPasswordVisible;
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, retrieve user profile
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                retrieveUserProfile(user.getUid());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerNowTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void retrieveUserProfile(String userId) {
        // Use "Users" to match the path in your Firebase database
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

                if (userProfile != null) {
                    // Pass user profile data to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", userProfile.getUsername());
                    intent.putExtra("bio", userProfile.getBio());
                    intent.putExtra("profileImageUrl", userProfile.getProfileImageUrl());
                    startActivity(intent);
                    finish(); // Close the login activity
                } else {
                    Toast.makeText(LoginActivity.this, "Profile not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Failed to retrieve profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            retrieveUserProfile(currentUser.getUid());
        }
    }
}
