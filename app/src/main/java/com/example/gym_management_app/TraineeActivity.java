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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;

public class TraineeActivity extends AppCompatActivity {

    private ImageView profileImage, ivTrainerPhoto;
    private TextView tvTraineeName, tvTrainerName;
    private TextView tvMembership, tvMissed, tvCalories, tvWorkout, tvWater, tvSleep;
    private FloatingActionButton fabFeatures;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        profileImage = findViewById(R.id.trainee_main_profile_image);
        tvTraineeName = findViewById(R.id.tv_trainee_name);
        ivTrainerPhoto = findViewById(R.id.iv_trainer_photo);
        tvTrainerName = findViewById(R.id.tv_trainer_name_display);
        fabFeatures = findViewById(R.id.fab_features);

        // Stats
        tvMembership = findViewById(R.id.tv_stat_membership);
        tvMissed = findViewById(R.id.tv_stat_missed);
        tvCalories = findViewById(R.id.tv_stat_calories);
        tvWorkout = findViewById(R.id.tv_stat_workout);
        tvWater = findViewById(R.id.tv_stat_water);
        tvSleep = findViewById(R.id.tv_stat_sleep);

        loadProfileData();

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeActivity.this, ProfileActivity.class);
            intent.putExtra("IS_TRAINEE", true);
            startActivity(intent);
        });

        fabFeatures.setOnClickListener(v -> {
            Intent intent = new Intent(TraineeActivity.this, FeaturesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        Cursor traineeCursor = dbHelper.getUserByEmail(currentUserEmail);
        if (traineeCursor != null && traineeCursor.moveToFirst()) {
            int traineeId = traineeCursor.getInt(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
            String name = traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            String gender = traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER));
            String trainerEmail = traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRAINER_EMAIL));
            
            tvTraineeName.setText("Welcome " + name);

            String imageUriStr = traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI));
            int avatarRes = traineeCursor.getInt(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR_RES));
            
            int defaultAvatar = (gender != null && gender.equalsIgnoreCase("Female")) ? R.drawable.red_female : R.drawable.img;
            loadCircularImage(imageUriStr, avatarRes <= 0 ? defaultAvatar : avatarRes, profileImage);

            if (trainerEmail != null && !trainerEmail.isEmpty()) {
                Cursor trainerCursor = dbHelper.getUserByEmail(trainerEmail);
                if (trainerCursor != null && trainerCursor.moveToFirst()) {
                    String tName = trainerCursor.getString(trainerCursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                    tvTrainerName.setText("Trainer: " + tName);
                    trainerCursor.close();
                }
            } else {
                tvTrainerName.setText("Trainer: Not Assigned");
            }
            
            setTraineeStatusImage(gender, traineeId);
            
            if (tvMembership != null) tvMembership.setText(traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEMBERSHIP_DAYS)));
            if (tvMissed != null) tvMissed.setText(traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSED_DAYS)));
            if (tvCalories != null) tvCalories.setText(traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALORIES)));
            if (tvWorkout != null) tvWorkout.setText(traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME)));
            if (tvWater != null) tvWater.setText(traineeCursor.getString(traineeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_INTAKE)));
            if (tvSleep != null) tvSleep.setText("08");
            
            traineeCursor.close();
        }
    }

    private void setTraineeStatusImage(String gender, int traineeId) {
        int[] maleImages = {R.drawable.red_male, R.drawable.yellow_male, R.drawable.green_male};
        int[] femaleImages = {R.drawable.red_female, R.drawable.green_female};

        int imageRes;
        if (gender != null && gender.equalsIgnoreCase("Female")) {
            imageRes = femaleImages[traineeId % femaleImages.length];
        } else {
            imageRes = maleImages[traineeId % maleImages.length];
        }
        
        ivTrainerPhoto.setImageResource(imageRes);
    }

    private void loadCircularImage(String uriStr, int resId, ImageView target) {
        if (uriStr != null && !uriStr.isEmpty()) {
            try {
                InputStream is = getContentResolver().openInputStream(Uri.parse(uriStr));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                target.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                target.setImageResource(resId);
            }
        } else {
            target.setImageResource(resId);
        }
    }
}