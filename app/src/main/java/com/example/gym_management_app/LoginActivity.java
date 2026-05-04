package com.example.gym_management_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);
        
        TextInputLayout emailLayout = findViewById(R.id.email_layout);
        TextInputLayout passLayout = findViewById(R.id.password_layout);
        TextInputEditText etEmail = (TextInputEditText) emailLayout.getEditText();
        TextInputEditText etPass = (TextInputEditText) passLayout.getEditText();

        SharedPreferences sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail == null || etPass == null) return;

                String email = etEmail.getText().toString();
                String password = etPass.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor cursor = databaseHelper.checkUser(email, password);
                if (cursor != null && cursor.moveToFirst()) {
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));

                    // Save session details
                    sharedPreferences.edit().putString("email", email).apply();
                    sharedPreferences.edit().putString("name", name).apply();
                    sharedPreferences.edit().putString("role", role).apply();

                    Intent intent;
                    if (role.contains("Owner")) {
                        intent = new Intent(LoginActivity.this, OwnerDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Check if the trainee's request is already accepted
                        String status = databaseHelper.getJoinRequestStatus(email);
                        if ("ACCEPTED".equals(status)) {
                            intent = new Intent(LoginActivity.this, TraineeActivity.class);
                            startActivity(intent);
                            finish();
                        } else if ("PENDING".equals(status)) {
                            Toast.makeText(LoginActivity.this, "Request already sent. Waiting for approval...", Toast.LENGTH_SHORT).show();
                        } else {
                            // If No request sent or Denied, go to join page
                            intent = new Intent(LoginActivity.this, TraineeDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    cursor.close();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    if(cursor != null) cursor.close();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}