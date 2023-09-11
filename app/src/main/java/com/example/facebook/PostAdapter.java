package com.example.facebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;

    public PostAdapter(Context context) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // Bind data to the views in the ViewHolder
        Post post = postList.get(position);

        // Example: Set text and image for a post
        holder.textViewPostText.setText(post.getPostText());
        // Load the image using an image loading library like Picasso or Glide
        // Example: Picasso.with(context).load(post.getImageUrl()).into(holder.imageViewPostImage);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPostText;
        ImageView imageViewPostImage;

        public PostViewHolder(View itemView) {
            super(itemView);
            textViewPostText = itemView.findViewById(R.id.textViewPostText);
            imageViewPostImage = itemView.findViewById(R.id.imageViewPostImage);
        }
    }

}
