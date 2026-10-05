package com.ramika.fixitdirect.activity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ramika.fixitdirect.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack, imgProfile;
    private EditText etFullName, etEmail, etPhone;
    private MaterialButton btnSaveProfile;
    private MaterialCardView btnChangePhoto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;

    private AlertDialog progressDialog;

    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    imgProfile.setImageBitmap(bitmap);
                    uploadImageToFirebase(bitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        imgProfile = findViewById(R.id.imgProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        etEmail.setText(currentUser.getEmail());
        btnBack.setOnClickListener(v -> finish());

        btnChangePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null);
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        setupProgressDialog();
        loadCurrentData();
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void loadCurrentData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String profilePicUrl = documentSnapshot.getString("profilePicUrl");

                        if (name != null) etFullName.setText(name);
                        if (phone != null) etPhone.setText(phone);

                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePicUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(imgProfile);
                        }
                    }
                });
    }


    private void setupProgressDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        progressBar.setIndeterminate(true);

        android.widget.TextView tvText = new android.widget.TextView(this);
        tvText.setText("Uploading Photo...");
        tvText.setTextSize(16);
        tvText.setPadding(50, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(tvText);

        builder.setView(layout);
        progressDialog = builder.create();
    }


    private void uploadImageToFirebase(Bitmap bitmap) {
        btnSaveProfile.setText("Uploading Photo...");
        btnSaveProfile.setEnabled(false);

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = storage.getReference().child("profile_images/" + userId + ".jpg");

        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {

            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                db.collection("users").document(userId)
                        .update("profilePicUrl", downloadUrl)
                        .addOnSuccessListener(aVoid -> {

                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            Toast.makeText(EditProfileActivity.this, "Profile Photo Updated!", Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setText("Save Changes");
                            btnSaveProfile.setEnabled(true);
                        });
            });
        }).addOnFailureListener(e -> {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            Log.e("EditProfileActivity", "Error uploading image", e);
            btnSaveProfile.setText("Save Changes");
            btnSaveProfile.setEnabled(true);
        });
    }

    private void saveProfileData() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            return;
        }

        btnSaveProfile.setText("Saving...");
        btnSaveProfile.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setText("Save Changes");
                    btnSaveProfile.setEnabled(true);
                });
    }
}