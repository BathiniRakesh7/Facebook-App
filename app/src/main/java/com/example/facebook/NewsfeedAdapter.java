package com.example.facebook;

import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.PostViewHolder> {

    private List<String> posts = new ArrayList<>();

    public NewsfeedAdapter() {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_newsfeed, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        String postText = posts.get(position);
        holder.postText.setText(postText);

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postText;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postText = itemView.findViewById(R.id.postText);
        }
    }
}
