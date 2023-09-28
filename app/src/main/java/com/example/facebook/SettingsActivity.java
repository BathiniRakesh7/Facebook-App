package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private EditText fullName, email, phoneNumber;
    private Button updateAccountSettings;
    private CircleImageView userprofileImage;
    private DocumentReference settingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId,saveCurrentDate,downloadUrl;
    private Uri ImageUri;
    private static final int Gallery_Pick = 1;

    private StorageReference profileImagesReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        settingsUserRef = db.collection("Users").document(currentUserId);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        profileImagesReference = storageReference.child("Profile Images");

        mToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fullName = findViewById(R.id.settings_full_name);
        email = findViewById(R.id.settings_email);
        phoneNumber = findViewById(R.id.settings_phone_number);
        updateAccountSettings = findViewById(R.id.update_account_settings_button);
        userprofileImage = findViewById(R.id.settings_profile_image);
        userprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


        settingsUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("FullName");
                    String userEmail = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String profileImage = documentSnapshot.getString("profileImage");
                    fullName.setText(userName);
                    email.setText(userEmail);
                    phoneNumber.setText(phone);
                    Picasso.get().load(profileImage).into(userprofileImage);
                } else {
                    Toast.makeText(SettingsActivity.this, "Data not Retrieved", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAccountInfo();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            userprofileImage.setImageURI(ImageUri);
        }
    }

    private void validateAccountInfo() {
        String name = fullName.getText().toString();
        String userEmail = email.getText().toString();
        String phone = phoneNumber.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your Full Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your Mobile Number", Toast.LENGTH_SHORT).show();
        } else {
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        String name = fullName.getText().toString();
        String userEmail = email.getText().toString();
        String phone = phoneNumber.getText().toString();

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());;

        StorageReference filePath = profileImagesReference.child(ImageUri.getLastPathSegment() + saveCurrentDate + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            Toast.makeText(SettingsActivity.this, "Image saved successfully...", Toast.LENGTH_SHORT).show();
                            updateAccountInfo(name, userEmail, phone);
                        }
                    });
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAccountInfo(String name, String userEmail, String phone) {

        HashMap userMap = new HashMap<>();
        userMap.put("FullName", name);
        userMap.put("email", userEmail);
        userMap.put("phone", phone);
        userMap.put("profileImage",downloadUrl);
        settingsUserRef.update(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendUserMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "error occur while updating", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void sendUserMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}