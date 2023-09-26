package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class MyPostsActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private CollectionReference postsRef;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();

        postsRef = FirebaseFirestore.getInstance().collection("Posts");

        mToolBar = findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        displayAllMyPosts();
    }

    private void displayAllMyPosts() {
        Query myPostsQuery = postsRef.whereEqualTo("uid", currentUserId);
        FirestoreRecyclerOptions<Posts> options = new FirestoreRecyclerOptions.Builder<Posts>()
                .setQuery(myPostsQuery, Posts.class)
                .build();

        FirestoreRecyclerAdapter<Posts,MyPostsActivity.MyPostsViewHolder> adapter =
                new FirestoreRecyclerAdapter<Posts, MyPostsActivity.MyPostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyPostsActivity.MyPostsViewHolder holder, int position, @NonNull Posts model) {
                        holder.setFullName(model.getFullName());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setPostImage(getApplicationContext(), model.getPostImage());
                        final String postId = getSnapshots().getSnapshot(position).getId();
                        loadPreviousLikes(postId, currentUserId, holder);


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("postId", postId);
                                startActivity(clickPostIntent);

                            }
                        });

                        holder.commentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsActivity = new Intent(MyPostsActivity.this, CommentsActivity.class);
                                commentsActivity.putExtra("postId", postId);
                                startActivity(commentsActivity);
                            }
                        });
                        holder.likeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                handleLikeClick(postId, currentUserId, holder);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public MyPostsActivity.MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_post_layout, parent, false);
                        return new MyPostsActivity.MyPostsViewHolder(view);
                    }
                };
        myPostsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ImageButton likeButton, commentButton;
        public TextView displayLike;
        public MyPostsViewHolder( View itemView) {
            super(itemView);
            mView = itemView;
            likeButton = mView.findViewById(R.id.like_post_btn);
            commentButton = mView.findViewById(R.id.comment_post_btn);
            displayLike = mView.findViewById(R.id.display_like_text);
        }
        public void setFullName(String fullName) {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullName);
        }

        public void setTime(String time) {
            TextView PostTime = mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date) {
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostImage(Context ctx1, String postImage) {
            ImageView PostImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(PostImage);
        }
    }

    public void loadPreviousLikes(final String postId, final String userId, final MyPostsActivity.MyPostsViewHolder holder) {
        final DocumentReference postLikesRef = firestore.collection("Likes").document(postId);

        postLikesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Map<String, Object> likesData = document.getData();
                        if (likesData != null) {
                            int currentLikes = likesData.size();
                            holder.displayLike.setText(currentLikes + " Likes");
                            boolean isLiked = isCurrentUserLiked(likesData, userId);
                            toggleLikeButtonAppearance(holder.likeButton, isLiked);
                        }
                    }
                } else {
                    Log.e("LoadPreviousLikes", "Firestore query failed: " + task.getException());
                }
            }
        });
    }

    private boolean isCurrentUserLiked(Map<String, Object> likesData, String userId) {
        return likesData.containsKey(userId);
    }

    public void handleLikeClick(final String postId, final String userId, final MyPostsActivity.MyPostsViewHolder holder) {
        final DocumentReference postLikesRef = firestore.collection("Likes").document(postId);
        holder.likeButton.setEnabled(false);

        postLikesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Map<String, Object> likesData = document.getData();
                        if (likesData != null && likesData.containsKey(userId)) {
                            postLikesRef.update(userId, FieldValue.delete());
                            int currentLikes = likesData.size() - 1;
                            holder.displayLike.setText(currentLikes + " Likes");
                            toggleLikeButtonAppearance(holder.likeButton, false);
                        } else {
                            postLikesRef.update(userId, true);
                            int currentLikes = likesData.size() + 1;
                            holder.displayLike.setText(currentLikes + " Likes");
                            toggleLikeButtonAppearance(holder.likeButton, true);
                        }
                    } else {
                        Map<String, Object> initialLike = new HashMap<>();
                        initialLike.put(userId, true);
                        postLikesRef.set(initialLike);
                        holder.displayLike.setText("1 Like");
                        toggleLikeButtonAppearance(holder.likeButton, true);
                    }
                } else {
                    Log.e("LikeClick", "Firestore query failed: " + task.getException());
                }
                holder.likeButton.setEnabled(true);
            }
        });
    }

    private void toggleLikeButtonAppearance(ImageButton likeButton, boolean liked) {
        if (liked) {
            likeButton.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }
}