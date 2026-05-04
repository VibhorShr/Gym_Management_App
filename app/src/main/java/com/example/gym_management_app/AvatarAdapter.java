package com.example.gym_management_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private List<Integer> avatarList;
    private OnAvatarClickListener listener;

    public interface OnAvatarClickListener {
        void onAvatarClick(int avatarResId);
    }

    public AvatarAdapter(List<Integer> avatarList, OnAvatarClickListener listener) {
        this.avatarList = avatarList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_item, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        int avatarResId = avatarList.get(position);
        holder.ivAvatar.setImageResource(avatarResId);
        holder.itemView.setOnClickListener(v -> listener.onAvatarClick(avatarResId));
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}