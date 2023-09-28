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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView userFullName;
    private ImageButton addNewPostButton;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private CollectionReference userRef;

    String currentUserId;


    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter<Posts, PostsViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = firestore.collection("Users");
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

        View navigationHeaderView = navigationView.inflateHeaderView(R.layout.navigation_header);

        postList = findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        displayAllPosts();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });
        userRef.document(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.getString("FullName");
                    String profileImage = snapshot.getString("profileImage");
                    TextView userFullName = findViewById(R.id.nav_user_full_name);
                    CircleImageView userProfile = findViewById(R.id.nav_profile_image);
                        userFullName.setText(userName);
                    Picasso.get().load(profileImage).into(userProfile);
                        Log.d("username", userName);

                } else {
                    Toast.makeText(MainActivity.this, "Profile name does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        });


        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserToPostActivity();

            }
        });

        updateUserStatus("online");
    }
    public void updateUserStatus(String state){
        String saveCurrentDate ,saveCurrentTime;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        Map<String,Object> currentStateMap = new HashMap<>();
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("type",state);

        userRef.document(currentUserId).update("userState",currentStateMap);
    }
    public void displayAllPosts(){

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
                String userId = model.getUid();
                loadUserProfileImage(userId, viewHolder.userProfileImageView);


                loadPreviousLikes(postId, currentUserId, viewHolder);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("postId", postId);
                        startActivity(clickPostIntent);

                    }
                });

                viewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent commentsActivity = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsActivity.putExtra("postId", postId);
                        startActivity(commentsActivity);
                    }
                });
                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleLikeClick(postId, currentUserId, viewHolder);

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

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ImageButton likeButton, commentButton;
        public TextView displayLike;
        public CircleImageView userProfileImageView;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            likeButton = mView.findViewById(R.id.like_post_btn);
            commentButton = mView.findViewById(R.id.comment_post_btn);
            displayLike = mView.findViewById(R.id.display_like_text);
            userProfileImageView = mView.findViewById(R.id.post_profile_image);
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
    private void loadUserProfileImage(String userId, CircleImageView userProfileImageView) {
        userRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImage = snapshot.getString("profileImage");
                    if (profileImage != null) {
                        Picasso.get().load(profileImage).into(userProfileImageView);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Profile image does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loadPreviousLikes(final String postId, final String userId, final PostsViewHolder viewHolder) {
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
                            viewHolder.displayLike.setText(currentLikes + " Likes");
                            boolean isLiked = isCurrentUserLiked(likesData, userId);
                            toggleLikeButtonAppearance(viewHolder.likeButton, isLiked);
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

    public void handleLikeClick(final String postId, final String userId, final PostsViewHolder viewHolder) {
        final DocumentReference postLikesRef = firestore.collection("Likes").document(postId);
        viewHolder.likeButton.setEnabled(false);

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
                            viewHolder.displayLike.setText(currentLikes + " Likes");
                            toggleLikeButtonAppearance(viewHolder.likeButton, false);
                        } else {
                            postLikesRef.update(userId, true);
                            int currentLikes = likesData.size() + 1;
                            viewHolder.displayLike.setText(currentLikes + " Likes");
                            toggleLikeButtonAppearance(viewHolder.likeButton, true);
                        }
                    } else {
                        Map<String, Object> initialLike = new HashMap<>();
                        initialLike.put(userId, true);
                        postLikesRef.set(initialLike);
                        viewHolder.displayLike.setText("1 Like");
                        toggleLikeButtonAppearance(viewHolder.likeButton, true);
                    }
                } else {
                    Log.e("LikeClick", "Firestore query failed: " + task.getException());
                }
                viewHolder.likeButton.setEnabled(true);
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
            sendUserToFriendsActivity();
            Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_find_friends) {
            sendUserToFindFriendsActivity();
            Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_messages) {
            sendUserToFriendsActivity();
            Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            sendUserToSettingsActivity();
            Toast.makeText(this, "Account Settings", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_Logout) {
            updateUserStatus("offline");
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginAccount.class));
            finish();
        }
    }

    private void sendUserToSettingsActivity() {
        Intent settingActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingActivity);
    }

    private void sendUserToFriendsActivity() {
        Intent friendsActivity = new Intent(this, FriendsActivity.class);
        startActivity(friendsActivity);
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
