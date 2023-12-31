package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private CollectionReference allFriendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String onlineUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        allFriendsRef = db.collection("Friends").document(onlineUserId).collection("acceptRequests");
        usersRef = db.collection("Users");


        myFriendList = findViewById(R.id.friend_list);

        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        displayAllFriends();
    }

    private void displayAllFriends() {
        FirestoreRecyclerOptions<Friends> options = new FirestoreRecyclerOptions.Builder<Friends>()
                .setQuery(allFriendsRef, Friends.class)
                .build();

        FirestoreRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder> adapter =
                new FirestoreRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FriendsActivity.FriendsViewHolder holder, int position, @NonNull Friends model) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
                        String usersId = snapshot.getId();
                        usersRef.document(usersId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String userName = documentSnapshot.getString("FullName");
                                    String profileImage = documentSnapshot.getString("profileImage");
                                    final String type;
                                    if (documentSnapshot.contains("userState")) {
                                        type = documentSnapshot.getString("userState.type");

                                        if (type.equals("online")) {
                                            holder.onlineStatus.setVisibility(View.VISIBLE);
                                        } else {
                                            holder.onlineStatus.setVisibility(View.INVISIBLE);

                                        }
                                    }
                                    holder.setFullName(userName);
                                    holder.setProfileImage(profileImage);

                                    holder.setDateTime(model.getDateTime());
                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CharSequence[] options = new CharSequence[]{
                                                    userName + "'s Profile",
                                                    "Send Message"
                                            };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                            builder.setTitle("Select an Option");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        Intent personProfileActivity = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                        personProfileActivity.putExtra("visitUserid", usersId);
                                                        startActivity(personProfileActivity);

                                                    }
                                                    if (i == 1) {
                                                        Intent chartActivity = new Intent(FriendsActivity.this, ChartActivity.class);
                                                        chartActivity.putExtra("visitUserid", usersId);
                                                        chartActivity.putExtra("userName", userName);
                                                        startActivity(chartActivity);

                                                    }

                                                }
                                            });
                                            builder.show();
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FriendsActivity.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_users_display_layout, parent, false);
                        return new FriendsActivity.FriendsViewHolder(view);
                    }
                };
        myFriendList.setAdapter(adapter);
        adapter.startListening();
    }

    public void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        Map<String, Object> currentStateMap = new HashMap<>();
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("type", state);

        usersRef.document(onlineUserId).update("userState", currentStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView onlineStatus;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatus = itemView.findViewById(R.id.all_users_online_icon);
        }

        public void setFullName(String FullName) {
            TextView userName = mView.findViewById(R.id.all_users_profile_name);
            userName.setText(FullName);
        }

        public void setDateTime(String dateTime) {
            TextView friendsDateTime = mView.findViewById(R.id.all_users_status);
            friendsDateTime.setText("Friends Since: " + dateTime);
        }

        public void setProfileImage(String profileImage) {
            ImageView profileImageView = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileImage).into(profileImageView);
        }

    }
}