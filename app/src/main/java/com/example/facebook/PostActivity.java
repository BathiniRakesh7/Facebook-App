package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private ImageButton selectPostImage;
    private Button uploadPostButton;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private EditText postDescription;
    private ProgressDialog uploadProgressBar;

    private StorageReference postsImagesReference;



    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id,Description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        selectPostImage = findViewById(R.id.select_post_image);
        uploadPostButton = findViewById(R.id.upload_post_button);
        postDescription = findViewById(R.id.post_description);
        mToolBar =findViewById(R.id.add_post_tool_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload Post");
        uploadProgressBar = new ProgressDialog(this);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        postsImagesReference = storageReference.child("Post Images");



        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenGallery();
            }
        });
        uploadPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    current_user_id = currentUser.getUid();
                } else {
                    Toast.makeText(PostActivity.this, "User is not authenticated.", Toast.LENGTH_SHORT).show();
                }
                ValidatePostInfo();
            }
        });

    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            selectPostImage.setImageURI(ImageUri);
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            SendUserMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    private void SavingPostInformationToFirestore() {
        if (current_user_id == null || current_user_id.isEmpty()) {
            Toast.makeText(PostActivity.this, "User ID is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(current_user_id);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userFullName = documentSnapshot.getString("FullName");

                            if (userFullName != null) {
                                Map<String, Object> post = new HashMap<>();
                                post.put("uid", current_user_id);
                                post.put("date", saveCurrentDate);
                                post.put("time", saveCurrentTime);
                                post.put("description", Description);
                                post.put("postImage", downloadUrl);
                                post.put("fullName", userFullName);

                                try {
                                    db.collection("Posts")
                                            .add(post)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    SendUserMainActivity();
                                                    Toast.makeText(PostActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                                    uploadProgressBar.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firestore", "Error adding document: " + e.getMessage(), e);
                                                    Toast.makeText(PostActivity.this, "Error Occurred while updating your post.", Toast.LENGTH_SHORT).show();
                                                    uploadProgressBar.dismiss();
                                                }
                                            });
                                } catch (Exception e) {
                                    Log.e("Firestore", "Error: " + e.getMessage(), e);
                                    Toast.makeText(PostActivity.this, "Error Occurred during Firestore operation.", Toast.LENGTH_SHORT).show();
                                    uploadProgressBar.dismiss();
                                }
                            } else {
                                Toast.makeText(PostActivity.this, "User's full name not found.", Toast.LENGTH_SHORT).show();
                                uploadProgressBar.dismiss();
                            }
                        } else {
                            Toast.makeText(PostActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                            uploadProgressBar.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting user data: " + e.getMessage(), e);
                        Toast.makeText(PostActivity.this, "Error Occurred while fetching user data.", Toast.LENGTH_SHORT).show();
                        uploadProgressBar.dismiss();
                    }
                });
    }




    private void ValidatePostInfo()
    {
        Description = postDescription.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            uploadProgressBar.setTitle("Add New Post");
            uploadProgressBar.setMessage("Please wait, while we are updating your new post...");
            uploadProgressBar.show();
            uploadProgressBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = postsImagesReference.child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

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
                            Toast.makeText(PostActivity.this, "Image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();
                            SavingPostInformationToFirestore();
                        }
                    });
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void SendUserMainActivity() {
        Intent mainIntent = new Intent(this,MainActivity.class);
        startActivity(mainIntent);
    }

}


