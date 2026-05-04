package com.example.gym_management_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;

public class TraineeDashboardActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvWelcome;
    private TextInputEditText etOwnerName, etCode;
    private Button btnJoin;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_dashboard);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        profileImage = findViewById(R.id.trainee_profile_image);
        tvWelcome = findViewById(R.id.tv_welcome_trainee);
        etOwnerName = findViewById(R.id.et_owner_name);
        etCode = findViewById(R.id.et_join_unique_code);
        btnJoin = findViewById(R.id.btn_join_trainer);

        loadProfileData();

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeDashboardActivity.this, ProfileActivity.class);
            intent.putExtra("IS_TRAINEE", true);
            startActivity(intent);
        });

        btnJoin.setOnClickListener(v -> {
            String trainerName = etOwnerName.getText().toString();
            String code = etCode.getText().toString();

            if (trainerName.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor trainerCursor = dbHelper.getTrainerByNameAndCode(trainerName, code);
            if (trainerCursor != null && trainerCursor.moveToFirst()) {
                String trainerEmail = trainerCursor.getString(trainerCursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                dbHelper.sendJoinRequest(currentUserEmail, trainerEmail);
                Toast.makeText(this, "Request sent. Please wait for approval.", Toast.LENGTH_LONG).show();
                trainerCursor.close();
                
                // Redirect back to login activity after sending request
                Intent intent = new Intent(TraineeDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Trainer not found or incorrect code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getUserByEmail(currentUserEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            tvWelcome.setText("Welcome " + name);

            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI));
            int avatarRes = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR_RES));

            if (imageUriStr != null) {
                try {
                    InputStream is = getContentResolver().openInputStream(Uri.parse(imageUriStr));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    profileImage.setImageBitmap(bitmap);
                    is.close();
                } catch (Exception e) {
                    profileImage.setImageResource(R.drawable.img);
                }
            } else {
                profileImage.setImageResource(avatarRes == 0 ? R.drawable.img : avatarRes);
            }
            cursor.close();
        }
    }
}