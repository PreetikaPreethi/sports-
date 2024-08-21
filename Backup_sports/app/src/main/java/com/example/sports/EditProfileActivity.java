package com.example.sports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextUsername, editTextBio;
    private ImageView imageViewProfile;
    private Button buttonUpdate;
    private ImageButton changeProfilePictureButton;
    private Uri profileImageUri;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        userRef = database.getReference("users");

        // Initialize views
        editTextUsername = findViewById(R.id.textViewUsername);
        editTextBio = findViewById(R.id.textViewBio);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonUpdate = findViewById(R.id.updateButton);
        changeProfilePictureButton = findViewById(R.id.changeProfilePictureButton);
        progressBar = findViewById(R.id.progressBar);

        // Load current user data
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            editTextUsername.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imageViewProfile);
            }
        }

        // Handle profile image selection
        changeProfilePictureButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
        });

        // Handle update button click
        buttonUpdate.setOnClickListener(v -> updateProfile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            imageViewProfile.setImageURI(profileImageUri);
        }
    }

    private void updateProfile() {
        String updatedUsername = editTextUsername.getText().toString();
        String updatedBio = editTextBio.getText().toString();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (profileImageUri != null) {
                progressBar.setVisibility(View.VISIBLE);
                buttonUpdate.setEnabled(false);
                // Upload profile image to Firebase Storage
                StorageReference profileImageRef = storage.getReference().child("profile_images").child(user.getUid() + ".jpg");
                profileImageRef.putFile(profileImageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileImageRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                            if (urlTask.isSuccessful()) {
                                String profileImageUrl = urlTask.getResult().toString();
                                updateUserProfile(user, updatedUsername, updatedBio, profileImageUrl);
                            } else {
                                showError("Failed to get profile image URL.");
                            }
                        });
                    } else {
                        showError("Failed to upload profile image.");
                    }
                });
            } else {
                updateUserProfile(user, updatedUsername, updatedBio, user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
            }
        }
    }

    private void updateUserProfile(FirebaseUser user, String username, String bio, String profileImageUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(profileImageUrl != null ? Uri.parse(profileImageUrl) : null)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            buttonUpdate.setEnabled(true);
            if (task.isSuccessful()) {
                updateUserDataInDatabase(username, bio, profileImageUrl);
            } else {
                showError("Profile update failed.");
            }
        });
    }

    private void updateUserDataInDatabase(String username, String bio, String profileImageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            UserProfile userProfile = new UserProfile(username, bio);
            userProfile.setProfileImageUrl(profileImageUrl);

            userRef.child(userId).setValue(userProfile).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("username", username);
                    resultIntent.putExtra("bio", bio);
                    resultIntent.putExtra("profileImageUrl", profileImageUrl);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    showError("Failed to update database.");
                }
            });
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        buttonUpdate.setEnabled(true);
        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
