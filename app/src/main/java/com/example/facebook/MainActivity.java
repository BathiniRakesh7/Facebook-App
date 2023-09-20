package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView userFullName;
    private ImageButton addNewPostButton;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private DatabaseReference userRef;

    String currentUserId;
    private int currentLikes = 0;



    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter<Posts, PostsViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = findViewById(R.id.main_page_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawable_layout);

        addNewPostButton = findViewById(R.id.add_new_post_button);


        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.navigation_view);
        userFullName = findViewById(R.id.nav_user_full_name);

        View navigationHeaderView = navigationView.inflateHeaderView(R.layout.navigation_header);

        postList = findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("FullName")) {

                        String userName = snapshot.child("FullName").getValue(String.class);
                        userFullName.setText(userName);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name do not exists", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserToPostActivity();

            }
        });


        Query query = firestore.collection("Posts");

        FirestoreRecyclerOptions<Posts> options = new FirestoreRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull Posts model) {
                viewHolder.setFullName(model.getFullName());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());
                final String postId = getSnapshots().getSnapshot(position).getId();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("postId",postId);
                        startActivity(clickPostIntent);

                    }
                });

            }
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);
                return new PostsViewHolder(view);
            }
        };

        postList.setAdapter(adapter);

    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.stopListening();
        adapter = null;

        // Re-create the adapter with new options.
        Query query = firestore.collection("Posts");
        FirestoreRecyclerOptions<Posts> options = new FirestoreRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull Posts model) {
                viewHolder.setFullName(model.getFullName());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());
                 String postId = getSnapshots().getSnapshot(position).getId();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("postId",postId);
                        startActivity(clickPostIntent);

                    }
                });

            }
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);
                return new PostsViewHolder(view);
            }
        };


        postList.setAdapter(adapter);
        adapter.startListening();
    }





    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public ImageView likeButton;
        public TextView likeCount;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            likeButton = mView.findViewById(R.id.like_btn);
            likeCount = mView.findViewById(R.id.like_text);
        }

        public void setLikeButtonState(boolean isLiked) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.baseline_favorite_24);
            } else {
                likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
            }
        }

        public void setLikes(int likes) {
            likeCount.setText(String.valueOf(likes));
        }

        public void setFullName(String fullName)
        {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullName);
        }
        public void setTime(String time)
        {
            TextView PostTime = mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostImage(Context ctx1, String postImage)
        {
            ImageView PostImage = mView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(PostImage);
        }
    }

    private void SendUserToPostActivity() {
        Intent addPostActivity = new Intent(this, PostActivity.class);
        startActivity(addPostActivity);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_profile) {
            sendUserToProfileActivity();
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_home) {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_friends) {
            Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_find_friends) {
            sendUserToFindFriendsActivity();
            Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_messages) {
            Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            sendUserToSettingsActivity();
            Toast.makeText(this, "Account Settings", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_Logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginAccount.class));
            finish();
        }
    }

    private void sendUserToSettingsActivity() {
        Intent settingActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingActivity);
    }
    private void sendUserToProfileActivity() {
        Intent profileActivity = new Intent(this, ProfileActivity.class);
        startActivity(profileActivity);
    }
    private void sendUserToFindFriendsActivity() {
        Intent findFriendsActivity = new Intent(this, FindFriendsActivity.class);
        startActivity(findFriendsActivity);
    }
}
