package com.example.gym_management_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<TraineeRequest> requests;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(TraineeRequest request);
    }

    public RequestsAdapter(List<TraineeRequest> requests, OnRequestClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        TraineeRequest request = requests.get(position);
        holder.tvName.setText(request.getName());
        holder.tvEmail.setText(request.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onRequestClick(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_request_trainee_name);
            tvEmail = itemView.findViewById(R.id.tv_request_trainee_email);
        }
    }
}