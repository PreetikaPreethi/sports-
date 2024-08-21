package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private DrawerLayout drawer;
    private ImageButton menuHamburger;
    private Button editProfileButton;
    private ImageView imageViewProfile;
    private TextView textViewUsername, textViewBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        menuHamburger = findViewById(R.id.menu_hamburger);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the ImageButton to open the drawer
        menuHamburger.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        // Load the default fragment (EventsFragment in this case)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new EventsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_events);
        }

        // Get the logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the header view of the NavigationView
            View headerView = navigationView.getHeaderView(0);

            // Find the TextViews and ImageView where the profile details will be displayed
            textViewUsername = headerView.findViewById(R.id.textViewUsername);
            textViewBio = headerView.findViewById(R.id.textViewBio);
            imageViewProfile = headerView.findViewById(R.id.imageViewProfile);

            // Set the username in the TextView
            String displayName = user.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";
            }
            textViewUsername.setText(displayName);

            // Set up the Edit button
            editProfileButton = headerView.findViewById(R.id.buttonEditProfile);
            editProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE); // Start for result
            });

            // Load the user's profile image if available
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).circleCrop().into(imageViewProfile);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_events) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new EventsFragment()).commit();
        } else if (itemId == R.id.nav_calendar) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CalendarFragment()).commit();
        } else if (itemId == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AboutFragment()).commit();
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String updatedUsername = data.getStringExtra("username");
            String updatedBio = data.getStringExtra("bio");
            String updatedProfileImageUrl = data.getStringExtra("profileImageUrl");

            if (updatedUsername != null) {
                textViewUsername.setText(updatedUsername);
            }
            if (updatedBio != null) {
                textViewBio.setText(updatedBio);
            }
            if (updatedProfileImageUrl != null) {
                Glide.with(this).load(updatedProfileImageUrl).circleCrop().into(imageViewProfile);
            }

            // Update Firebase with new profile data
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                if (updatedUsername != null) {
                    userRef.child("username").setValue(updatedUsername);
                }
                if (updatedBio != null) {
                    userRef.child("bio").setValue(updatedBio);
                }
                if (updatedProfileImageUrl != null) {
                    userRef.child("profileImageUrl").setValue(updatedProfileImageUrl);
                }
            }
        }
    }
}
