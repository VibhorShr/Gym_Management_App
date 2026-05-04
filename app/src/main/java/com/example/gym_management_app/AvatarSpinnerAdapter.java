package com.example.gym_management_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AvatarSpinnerAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> avatarList;
    private List<String> avatarNames;

    public AvatarSpinnerAdapter(Context context, List<Integer> avatarList, List<String> avatarNames) {
        this.context = context;
        this.avatarList = avatarList;
        this.avatarNames = avatarNames;
    }

    @Override
    public int getCount() {
        return avatarList.size();
    }

    @Override
    public Object getItem(int position) {
        return avatarList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.avatar_item, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.iv_avatar);
        TextView textView = convertView.findViewById(R.id.tv_avatar_name);
        
        imageView.setImageResource(avatarList.get(position));
        textView.setText(avatarNames.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}