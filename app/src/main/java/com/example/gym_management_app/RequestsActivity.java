package com.example.gym_management_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private TextView tvNoRequests;
    private DatabaseHelper dbHelper;
    private String currentOwnerEmail;
    private List<TraineeRequest> requestList;
    private RequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentOwnerEmail = prefs.getString("email", "");

        rvRequests = findViewById(R.id.rv_pending_requests);
        tvNoRequests = findViewById(R.id.tv_no_requests);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        
        adapter = new RequestsAdapter(requestList, new RequestsAdapter.OnRequestClickListener() {
            @Override
            public void onRequestClick(TraineeRequest request) {
                showActionDialog(request);
            }
        });
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        requestList.clear();
        Cursor cursor = dbHelper.getPendingRequests(currentOwnerEmail);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REQ_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                requestList.add(new TraineeRequest(id, name, email));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (requestList.isEmpty()) {
            tvNoRequests.setVisibility(View.VISIBLE);
            rvRequests.setVisibility(View.GONE);
        } else {
            tvNoRequests.setVisibility(View.GONE);
            rvRequests.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showActionDialog(TraineeRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Request");
        builder.setMessage("Do you want to accept " + request.getName() + " as your trainee?");
        
        builder.setPositiveButton("Accept", (dialog, which) -> {
            dbHelper.updateRequestStatus(request.getId(), request.getEmail(), currentOwnerEmail, "ACCEPTED");
            Toast.makeText(this, "Request Accepted!", Toast.LENGTH_SHORT).show();
            loadRequests();
        });

        builder.setNegativeButton("Decline", (dialog, which) -> {
            dbHelper.updateRequestStatus(request.getId(), request.getEmail(), currentOwnerEmail, "DENIED");
            Toast.makeText(this, "Request Declined", Toast.LENGTH_SHORT).show();
            loadRequests();
        });

        builder.show();
    }
}