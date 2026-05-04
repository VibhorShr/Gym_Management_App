package com.example.gym_management_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivCurrentProfile;
    private Spinner avatarSpinner;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private boolean isInitialSelection = true;

    private TextInputEditText etName, etEmail, etPassword, etAddress, etNumber, etAge, etWeight, etHeight, etGender, etUniqueCode;
    private TextInputLayout layoutUniqueCode;
    private Button btnSave;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        // Initialize UI elements
        ivCurrentProfile = findViewById(R.id.iv_current_profile);
        avatarSpinner = findViewById(R.id.avatar_spinner);
        etName = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_name_layout)).getEditText();
        etEmail = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_email_layout)).getEditText();
        etPassword = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_password_layout)).getEditText();
        etAddress = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_address_layout)).getEditText();
        etNumber = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_number_layout)).getEditText();
        etAge = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_age_layout)).getEditText();
        etWeight = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_weight_layout)).getEditText();
        etHeight = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_height_layout)).getEditText();
        etGender = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_gender_layout)).getEditText();
        etUniqueCode = (TextInputEditText) ((TextInputLayout) findViewById(R.id.profile_unique_code_layout)).getEditText();
        layoutUniqueCode = findViewById(R.id.profile_unique_code_layout);
        btnSave = findViewById(R.id.btn_save_profile);

        // Handle Trainee specific UI: Hide unique code
        if (getIntent().getBooleanExtra("IS_TRAINEE", false)) {
            layoutUniqueCode.setVisibility(View.GONE);
        }

        // Load saved data from DATABASE (Specific to currentUserEmail)
        loadProfileData();

        // Setup Avatar List for Spinner
        List<Integer> avatars = new ArrayList<>();
        List<String> avatarNames = new ArrayList<>();
        avatars.add(R.drawable.img); avatarNames.add("Master");
        avatars.add(R.drawable.avtar1); avatarNames.add("Beast");
        avatars.add(R.drawable.avtar2); avatarNames.add("Titan");
        avatars.add(R.drawable.avtar3); avatarNames.add("Warrior");
        avatars.add(R.drawable.avtar4); avatarNames.add("Champion");
        avatars.add(R.drawable.avtar5); avatarNames.add("Hulk");
        avatars.add(R.drawable.avtar6); avatarNames.add("Legend");
        avatars.add(R.drawable.avtar7); avatarNames.add("Spartan");
        avatars.add(android.R.drawable.ic_menu_gallery); avatarNames.add("Gallery");

        AvatarSpinnerAdapter adapter = new AvatarSpinnerAdapter(this, avatars, avatarNames);
        avatarSpinner.setAdapter(adapter);

        ivCurrentProfile.setOnClickListener(v -> avatarSpinner.performClick());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            final int takeFlags = result.getData().getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                        } catch (Exception e) {}

                        updateProfileImageUI(imageUri.toString(), -1);
                        // Save temporarily in DB helper values
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COL_IMAGE_URI, imageUri.toString());
                        values.put(DatabaseHelper.COL_AVATAR_RES, -1);
                        dbHelper.updateUserDetails(currentUserEmail, values);
                    }
                }
        );

        avatarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSelection) {
                    isInitialSelection = false;
                    return;
                }

                if (position == avatars.size() - 1) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    galleryLauncher.launch(intent);
                } else {
                    int selectedAvatar = avatars.get(position);
                    updateProfileImageUI(null, selectedAvatar);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COL_IMAGE_URI, (String)null);
                    values.put(DatabaseHelper.COL_AVATAR_RES, selectedAvatar);
                    dbHelper.updateUserDetails(currentUserEmail, values);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> {
            saveProfileData();
            Toast.makeText(ProfileActivity.this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateProfileImageUI(String uriStr, int resId) {
        if (uriStr != null) {
            try {
                InputStream is = getContentResolver().openInputStream(Uri.parse(uriStr));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ivCurrentProfile.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                ivCurrentProfile.setImageResource(R.drawable.img);
            }
        } else {
            ivCurrentProfile.setImageResource(resId != -1 ? resId : R.drawable.img);
        }
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getUserByEmail(currentUserEmail);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)));
            etPassword.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
            etNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE)));
            etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AGE)));
            etWeight.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEIGHT)));
            etHeight.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEIGHT)));
            etGender.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER)));
            etUniqueCode.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIQUE_CODE)));

            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI));
            int avatarRes = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR_RES));
            updateProfileImageUI(imageUriStr, avatarRes == 0 ? R.drawable.img : avatarRes);
            cursor.close();
        }
    }

    private void saveProfileData() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_FULL_NAME, etName.getText().toString());
        values.put(DatabaseHelper.COL_PASSWORD, etPassword.getText().toString());
        values.put(DatabaseHelper.COL_ADDRESS, etAddress.getText().toString());
        values.put(DatabaseHelper.COL_PHONE, etNumber.getText().toString());
        values.put(DatabaseHelper.COL_AGE, etAge.getText().toString());
        values.put(DatabaseHelper.COL_WEIGHT, etWeight.getText().toString());
        values.put(DatabaseHelper.COL_HEIGHT, etHeight.getText().toString());
        values.put(DatabaseHelper.COL_GENDER, etGender.getText().toString());
        values.put(DatabaseHelper.COL_UNIQUE_CODE, etUniqueCode.getText().toString());

        dbHelper.updateUserDetails(currentUserEmail, values);
        
        // Also update name in shared prefs for headers
        sharedPreferences.edit().putString("name", etName.getText().toString()).apply();
    }
}