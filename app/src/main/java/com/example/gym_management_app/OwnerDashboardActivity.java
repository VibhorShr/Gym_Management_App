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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OwnerDashboardActivity extends AppCompatActivity {

    private ImageView profileImage, ivNotifications;
    private TextView tvWelcome, tvTotalTrainees, tvPendingRequests;
    private View notificationDot;
    private RecyclerView rvTrainees;
    private TraineeAdapter adapter;
    private List<TraineeModel> traineeList;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private String currentOwnerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentOwnerEmail = sharedPreferences.getString("email", "");

        profileImage = findViewById(R.id.profile_image);
        tvWelcome = findViewById(R.id.tv_welcome);
        ivNotifications = findViewById(R.id.iv_notifications);
        notificationDot = findViewById(R.id.notification_dot);
        tvTotalTrainees = findViewById(R.id.tv_total_trainees);
        tvPendingRequests = findViewById(R.id.tv_pending_requests);
        rvTrainees = findViewById(R.id.rv_my_trainees);

        rvTrainees.setLayoutManager(new LinearLayoutManager(this));
        traineeList = new ArrayList<>();
        
        adapter = new TraineeAdapter(traineeList, new TraineeAdapter.OnUpdateClickListener() {
            @Override
            public void onUpdateClick(TraineeModel trainee) {
                showUpdateDialog(trainee);
            }
        });
        rvTrainees.setAdapter(adapter);

        loadProfileData();
        updateDashboardStats();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerDashboardActivity.this, ProfileActivity.class);
                intent.putExtra("IS_TRAINEE", false);
                startActivity(intent);
            }
        });

        ivNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerDashboardActivity.this, RequestsActivity.class);
                startActivity(intent);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OwnerDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        loadTrainees();
        
        tvTotalTrainees.setText(String.valueOf(traineeList.size()));

        int pendingCount = 0;
        Cursor cursor = dbHelper.getPendingRequests(currentOwnerEmail);
        if (cursor != null) {
            pendingCount = cursor.getCount();
            cursor.close();
        }
        tvPendingRequests.setText(String.valueOf(pendingCount));

        if (pendingCount > 0) {
            notificationDot.setVisibility(View.VISIBLE);
        } else {
            notificationDot.setVisibility(View.GONE);
        }
    }

    private void loadTrainees() {
        traineeList.clear();
        Cursor cursor = dbHelper.getMyTrainees(currentOwnerEmail);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GENDER));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String membership = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MEMBERSHIP_DAYS));
                String missed = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSED_DAYS));
                String calories = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALORIES));
                String workout = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                String water = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_INTAKE));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI));
                int avatarRes = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR_RES));

                traineeList.add(new TraineeModel(id, name, email, gender, address, phone, membership, missed, calories, workout, water, imageUri, avatarRes));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(TraineeModel trainee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_progress, null);
        builder.setView(dialogView);

        EditText etMembership = dialogView.findViewById(R.id.et_update_membership);
        EditText etMissed = dialogView.findViewById(R.id.et_update_missed);
        EditText etCalories = dialogView.findViewById(R.id.et_update_calories);
        EditText etWorkout = dialogView.findViewById(R.id.et_update_workout);

        etMembership.setText(trainee.getMembershipDays());
        etMissed.setText(trainee.getMissedDays());
        etCalories.setText(trainee.getCalories());
        etWorkout.setText(trainee.getWorkoutTime());

        builder.setTitle("Update Progress: " + trainee.getFullName());
        builder.setPositiveButton("Save", (dialog, which) -> {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_MEMBERSHIP_DAYS, etMembership.getText().toString());
            values.put(DatabaseHelper.COL_MISSED_DAYS, etMissed.getText().toString());
            values.put(DatabaseHelper.COL_CALORIES, etCalories.getText().toString());
            values.put(DatabaseHelper.COL_WORKOUT_TIME, etWorkout.getText().toString());

            dbHelper.updateUserDetails(trainee.getEmail(), values);
            Toast.makeText(this, "Progress Updated!", Toast.LENGTH_SHORT).show();
            updateDashboardStats();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getUserByEmail(currentOwnerEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI));
            int avatarRes = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR_RES));

            if (avatarRes <= 0) {
                avatarRes = R.drawable.img;
            }

            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    Uri uri = Uri.parse(imageUriStr);
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (is != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        profileImage.setImageBitmap(bitmap);
                        is.close();
                    } else {
                        profileImage.setImageResource(avatarRes);
                    }
                } catch (Exception e) {
                    profileImage.setImageResource(avatarRes);
                }
            } else {
                profileImage.setImageResource(avatarRes);
            }

            tvWelcome.setText("Welcome " + name);
            cursor.close();
        } else {
            profileImage.setImageResource(R.drawable.img);
            tvWelcome.setText("Welcome Owner");
        }
    }
}
