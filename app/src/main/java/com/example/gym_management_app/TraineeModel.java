package com.example.gym_management_app;

public class TraineeModel {
    private int id;
    private String fullName;
    private String email;
    private String gender;
    private String address;
    private String phone;
    private String membershipDays;
    private String missedDays;
    private String calories;
    private String workoutTime;
    private String waterIntake;
    private String imageUri;
    private int avatarRes;

    public TraineeModel(int id, String fullName, String email, String gender, String address, String phone, String membershipDays, String missedDays, String calories, String workoutTime, String waterIntake, String imageUri, int avatarRes) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.membershipDays = membershipDays;
        this.missedDays = missedDays;
        this.calories = calories;
        this.workoutTime = workoutTime;
        this.waterIntake = waterIntake;
        this.imageUri = imageUri;
        this.avatarRes = avatarRes;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getMembershipDays() { return membershipDays; }
    public String getMissedDays() { return missedDays; }
    public String getCalories() { return calories; }
    public String getWorkoutTime() { return workoutTime; }
    public String getWaterIntake() { return waterIntake; }
    public String getImageUri() { return imageUri; }
    public int getAvatarRes() { return avatarRes; }
}
