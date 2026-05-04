package com.example.gym_management_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TraineeAdapter extends RecyclerView.Adapter<TraineeAdapter.TraineeViewHolder> {

    private List<TraineeModel> traineeList;
    private OnUpdateClickListener listener;

    public interface OnUpdateClickListener {
        void onUpdateClick(TraineeModel trainee);
    }

    public TraineeAdapter(List<TraineeModel> traineeList, OnUpdateClickListener listener) {
        this.traineeList = traineeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TraineeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainee_item, parent, false);
        return new TraineeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TraineeViewHolder holder, int position) {
        TraineeModel trainee = traineeList.get(position);
        
        // Fixed Details
        holder.tvName.setText(trainee.getFullName());
        holder.tvEmail.setText("Email: " + trainee.getEmail());
        holder.tvPhone.setText("Phone: " + (trainee.getPhone() != null ? trainee.getPhone() : "N/A"));
        holder.tvGender.setText("Gender: " + (trainee.getGender() != null ? trainee.getGender() : "Not Selected"));
        holder.tvAddress.setText("Address: " + (trainee.getAddress() != null ? trainee.getAddress() : "N/A"));

        // Progress Stats
        holder.tvMembership.setText(trainee.getMembershipDays());
        holder.tvMissed.setText(trainee.getMissedDays());
        holder.tvCalories.setText(trainee.getCalories());
        holder.tvWorkout.setText(trainee.getWorkoutTime());

        // Set the status image based on gender and ID
        setTraineeStatusImage(holder.ivTraineePhoto, trainee.getGender(), trainee.getId());

        // Handle Expand/Collapse
        holder.ivArrow.setOnClickListener(v -> {
            boolean isExpanded = holder.expandedContent.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                holder.expandedContent.setVisibility(View.GONE);
                holder.ivArrow.setImageResource(android.R.drawable.arrow_down_float);
            } else {
                holder.expandedContent.setVisibility(View.VISIBLE);
                holder.ivArrow.setImageResource(android.R.drawable.arrow_up_float);
            }
        });

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdateClick(trainee));
    }

    private void setTraineeStatusImage(ImageView imageView, String gender, int traineeId) {
        int[] maleImages = {R.drawable.red_male, R.drawable.yellow_male, R.drawable.green_male};
        int[] femaleImages = {R.drawable.red_female, R.drawable.green_female};

        int imageRes;
        if (gender == null || gender.isEmpty()) {
            imageRes = R.drawable.red_male; // Default if no gender selected
        } else if (gender.equalsIgnoreCase("Female")) {
            imageRes = femaleImages[traineeId % femaleImages.length];
        } else {
            // Male or other
            imageRes = maleImages[traineeId % maleImages.length];
        }
        imageView.setImageResource(imageRes);
    }

    @Override
    public int getItemCount() {
        return traineeList.size();
    }

    static class TraineeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvGender, tvAddress;
        TextView tvMembership, tvMissed, tvCalories, tvWorkout;
        ImageView ivArrow, ivTraineePhoto;
        ConstraintLayout expandedContent;
        View btnUpdate;

        public TraineeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_trainee_name);
            tvEmail = itemView.findViewById(R.id.tv_card_email);
            tvPhone = itemView.findViewById(R.id.tv_card_phone);
            tvGender = itemView.findViewById(R.id.tv_card_gender);
            tvAddress = itemView.findViewById(R.id.tv_card_address);
            
            tvMembership = itemView.findViewById(R.id.tv_card_membership);
            tvMissed = itemView.findViewById(R.id.tv_card_missed);
            tvCalories = itemView.findViewById(R.id.tv_card_calories);
            tvWorkout = itemView.findViewById(R.id.tv_card_workout);

            ivArrow = itemView.findViewById(R.id.iv_expand_arrow);
            ivTraineePhoto = itemView.findViewById(R.id.iv_trainee_card_photo);
            expandedContent = itemView.findViewById(R.id.expanded_content);
            btnUpdate = itemView.findViewById(R.id.btn_edit_trainee_stats);
        }
    }
}
