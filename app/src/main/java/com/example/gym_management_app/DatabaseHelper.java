package com.example.gym_management_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GymManagement.db";
    private static final int DATABASE_VERSION = 7; // Incremented to version 7 to reset all data

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "ID";
    public static final String COL_FULL_NAME = "FULL_NAME";
    public static final String COL_EMAIL = "EMAIL";
    public static final String COL_PASSWORD = "PASSWORD";
    public static final String COL_ROLE = "ROLE";
    public static final String COL_ADDRESS = "ADDRESS";
    public static final String COL_PHONE = "PHONE";
    public static final String COL_AGE = "AGE";
    public static final String COL_WEIGHT = "WEIGHT";
    public static final String COL_HEIGHT = "HEIGHT";
    public static final String COL_GENDER = "GENDER";
    public static final String COL_UNIQUE_CODE = "UNIQUE_CODE";
    public static final String COL_IMAGE_URI = "IMAGE_URI";
    public static final String COL_AVATAR_RES = "AVATAR_RES";
    public static final String COL_TRAINER_EMAIL = "TRAINER_EMAIL"; 

    // Progress Columns
    public static final String COL_MEMBERSHIP_DAYS = "MEMBERSHIP_DAYS";
    public static final String COL_MISSED_DAYS = "MISSED_DAYS";
    public static final String COL_CALORIES = "CALORIES";
    public static final String COL_WORKOUT_TIME = "WORKOUT_TIME";
    public static final String COL_WATER_INTAKE = "WATER_INTAKE";

    // Requests Table
    public static final String TABLE_REQUESTS = "requests";
    public static final String COL_REQ_ID = "REQ_ID";
    public static final String COL_TRAINEE_EMAIL = "TRAINEE_EMAIL_REQ";
    public static final String COL_TRAINER_EMAIL_REQ = "TRAINER_EMAIL_TARGET";
    public static final String COL_STATUS = "STATUS"; 

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FULL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_ROLE + " TEXT, " +
                COL_ADDRESS + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_AGE + " TEXT, " +
                COL_WEIGHT + " TEXT, " +
                COL_HEIGHT + " TEXT, " +
                COL_GENDER + " TEXT, " +
                COL_UNIQUE_CODE + " TEXT, " +
                COL_IMAGE_URI + " TEXT, " +
                COL_AVATAR_RES + " INTEGER, " +
                COL_TRAINER_EMAIL + " TEXT, " +
                COL_MEMBERSHIP_DAYS + " TEXT DEFAULT '0/30', " +
                COL_MISSED_DAYS + " TEXT DEFAULT '0', " +
                COL_CALORIES + " TEXT DEFAULT '0', " +
                COL_WORKOUT_TIME + " TEXT DEFAULT '0', " +
                COL_WATER_INTAKE + " TEXT DEFAULT '0')";
        db.execSQL(createUsersTable);

        String createRequestsTable = "CREATE TABLE " + TABLE_REQUESTS + " (" +
                COL_REQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRAINEE_EMAIL + " TEXT, " +
                COL_TRAINER_EMAIL_REQ + " TEXT, " +
                COL_STATUS + " TEXT)";
        db.execSQL(createRequestsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop everything to start fresh
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS);
        onCreate(db);
    }

    public long addUser(String fullName, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FULL_NAME, fullName);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_ROLE, role);
        return db.insert(TABLE_USERS, null, contentValues);
    }

    public Cursor checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?", new String[]{email, password});
    }

    public boolean updateUserDetails(String email, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return result > 0;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?", new String[]{email});
    }

    public Cursor getTrainerByNameAndCode(String trainerName, String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_FULL_NAME + "=? AND " + COL_UNIQUE_CODE + "=? AND " + COL_ROLE + " LIKE '%Owner%'", new String[]{trainerName, code});
    }

    public long sendJoinRequest(String traineeEmail, String trainerEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRAINEE_EMAIL, traineeEmail);
        values.put(COL_TRAINER_EMAIL_REQ, trainerEmail);
        values.put(COL_STATUS, "PENDING");
        return db.insert(TABLE_REQUESTS, null, values);
    }

    public Cursor getPendingRequests(String trainerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT r." + COL_REQ_ID + ", u." + COL_FULL_NAME + ", u." + COL_EMAIL + " FROM " + TABLE_REQUESTS + " r JOIN " + TABLE_USERS + " u ON r." + COL_TRAINEE_EMAIL + " = u." + COL_EMAIL + " WHERE r." + COL_TRAINER_EMAIL_REQ + "=? AND r." + COL_STATUS + "='PENDING'", new String[]{trainerEmail});
    }

    public void updateRequestStatus(int requestId, String traineeEmail, String trainerEmail, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        db.update(TABLE_REQUESTS, values, COL_REQ_ID + "=?", new String[]{String.valueOf(requestId)});

        if (status.equals("ACCEPTED")) {
            ContentValues userValues = new ContentValues();
            userValues.put(COL_TRAINER_EMAIL, trainerEmail);
            db.update(TABLE_USERS, userValues, COL_EMAIL + "=?", new String[]{traineeEmail});
        }
    }

    public String getJoinRequestStatus(String traineeEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_STATUS + " FROM " + TABLE_REQUESTS + " WHERE " + COL_TRAINEE_EMAIL + "=? ORDER BY " + COL_REQ_ID + " DESC LIMIT 1", new String[]{traineeEmail});
        String status = null;
        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(0);
            cursor.close();
        }
        return status;
    }

    public Cursor getMyTrainees(String trainerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_TRAINER_EMAIL + "=?", new String[]{trainerEmail});
    }

    // DEBUG METHOD: Call this to see all users in Logcat
    public void debugPrintAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE));
                Log.d("DB_DEBUG", "Email: " + email + " | Pass: " + password + " | Role: " + role);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
