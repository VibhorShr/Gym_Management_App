package com.example.gym_management_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        final TextInputLayout nameLayout = findViewById(R.id.name_layout);
        final TextInputLayout emailLayout = findViewById(R.id.reg_email_layout);
        final TextInputLayout passLayout = findViewById(R.id.reg_password_layout);

        final TextInputEditText etName = (TextInputEditText) nameLayout.getEditText();
        final TextInputEditText etEmail = (TextInputEditText) emailLayout.getEditText();
        final TextInputEditText etPass = (TextInputEditText) passLayout.getEditText();

        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvLoginBack = findViewById(R.id.tv_login_back);
        RadioGroup rgRole = findViewById(R.id.rg_role);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName == null || etEmail == null || etPass == null) return;

                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String pass = etPass.getText().toString();
                
                int selectedId = rgRole.getCheckedRadioButtonId();
                if (selectedId == -1 || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all details and select a role", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton selectedButton = findViewById(selectedId);
                    String role = selectedButton.getText().toString();
                    
                    long id = databaseHelper.addUser(name, email, pass, role);
                    if (id != -1) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        if (role.contains("Owner")) {
                            Toast.makeText(RegisterActivity.this, "Complete your profile first", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, OwnerDashboardActivity.class);
                            startActivity(intent);
                        } else {
                            finish(); // Go to login for Trainee
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed or User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvLoginBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}