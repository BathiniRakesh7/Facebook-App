package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileName,profileEmail,profilePhoneNumber,profileStatus;
    private CircleImageView myProfileImage;
    private DocumentReference profileUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        profileUserRef = db.collection("Users").document(currentUserId);

        profileName =findViewById(R.id.my_profile_full_name);
        profileEmail =findViewById(R.id.my_profile_email);
        profilePhoneNumber =findViewById(R.id.my_profile_mobile);
        profileStatus =findViewById(R.id.my_profile_status);
        myProfileImage =findViewById(R.id.my_profile_image);

        profileUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("FullName");
                    String userEmail = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    profileName.setText(userName);
                    profileEmail.setText(userEmail);
                    profilePhoneNumber.setText(phone);
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
}