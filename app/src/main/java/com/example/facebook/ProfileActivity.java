package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileName, profileEmail, profilePhoneNumber, profileStatus;
    private CircleImageView myProfileImage;
    private DocumentReference profileUserRef;
    private CollectionReference friendsRef, postsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Button myPosts, myFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        profileUserRef = db.collection("Users").document(currentUserId);
        friendsRef = db.collection("Friends");
        postsRef = db.collection("Posts");

        profileName = findViewById(R.id.my_profile_full_name);
        profileEmail = findViewById(R.id.my_profile_email);
        profilePhoneNumber = findViewById(R.id.my_profile_mobile);
        profileStatus = findViewById(R.id.my_profile_status);
        myProfileImage = findViewById(R.id.my_profile_image);
        myPosts = findViewById(R.id.my_post_button);
        myFriends = findViewById(R.id.my_friends_button);

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToFriendsActivity();

            }
        });
        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToMyPostsActivity();

            }
        });
        postsRef.whereEqualTo("uid", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int postsCount = querySnapshot.size();
                        myPosts.setText(postsCount + " Posts");
                    } else {
                        myPosts.setText("0 Posts");
                    }
                });

        friendsRef.document(currentUserId).collection("acceptRequests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            int friendsCount = querySnapshot.size();
                            myFriends.setText(friendsCount + " Friends");
                        } else {
                            myFriends.setText("0 Friends");
                        }

                    }
                });
        profileUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("FullName");
                    String userEmail = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String profileImage = documentSnapshot.getString("profileImage");
                    profileName.setText(userName);
                    profileEmail.setText(userEmail);
                    profilePhoneNumber.setText(phone);
                    Picasso.get().load(profileImage).into(myProfileImage);
                } else {
                    Toast.makeText(ProfileActivity.this, "Data not Retrieved", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void sendUserToFriendsActivity() {
        Intent friendsActivity = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsActivity);
    }

    private void sendUserToMyPostsActivity() {
        Intent myPostsActivity = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(myPostsActivity);
    }
}