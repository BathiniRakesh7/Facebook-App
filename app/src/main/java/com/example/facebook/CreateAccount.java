package com.example.facebook;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    public static final String TAG = "Tag";
    private static final int Gallery_Pick = 1;

    private EditText fullName, email, password, phone;
    private Button registerBtn;
    private TextView loginBtn;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private FirebaseFirestore store;
    private String userID,saveCurrentDateTime,downloadUrl;
    private ImageView profileImageView;
    private Uri selectedImageUri;
    private StorageReference profileImagesReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        profileImagesReference = storageReference.child("Profile Images");

        fullName = findViewById(R.id.editTextname);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        phone = findViewById(R.id.editTextPhone);
        registerBtn = findViewById(R.id.Registerbtn);
        loginBtn = findViewById(R.id.textCreateHere);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        store =FirebaseFirestore.getInstance();
        profileImageView = findViewById(R.id.profileImageView);



        if (auth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();

        }


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginAccount.class));

            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });
    }

    private void saveUserDetails() {
        String email = CreateAccount.this.email.getText().toString().trim();
        String password = CreateAccount.this.password.getText().toString().trim();
        String phone = CreateAccount.this.phone.getText().toString();
        String name = fullName.getText().toString();

        if (TextUtils.isEmpty(email)){
            CreateAccount.this.email.setError("Email is Required");
            return;
        }
        if (TextUtils.isEmpty(password)){
            CreateAccount.this.password.setError("Password is Required");
            return;
        }
        if (password.length()<6){
            CreateAccount.this.password.setError("Password must be above 6 Characters");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    Toast.makeText(CreateAccount.this, "User Created", Toast.LENGTH_SHORT).show();
                    userID = auth.getCurrentUser().getUid();
                    DocumentReference documentReference = store.collection("Users").document(userID);
                    Map<String,Object> user = new HashMap<>();
                    user.put("FullName",name);
                    user.put("email",email);
                    user.put("phone",phone);
                    user.put("profileImage", downloadUrl);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG ,"OnSuccess : user profile is created for"+ userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG ,"OnFailure :" + e.toString());
                        }
                    });
                    startActivity(new Intent(getApplicationContext(),LoginAccount.class));

                }
                else {
                    Toast.makeText(CreateAccount.this, "Error"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                }


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

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void validatePostInfo()
    {

        if(selectedImageUri == null)
        {
            Toast.makeText(this, "Please select profile image...", Toast.LENGTH_SHORT).show();
        }else {
            StoringImageToFirebaseStorage();

        }

    }


    private void StoringImageToFirebaseStorage()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDateTime = currentDate.format(calFordDate.getTime());;

        StorageReference filePath = profileImagesReference.child(selectedImageUri.getLastPathSegment() + saveCurrentDateTime + ".jpg");

        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            Toast.makeText(CreateAccount.this, "Image saved successfully...", Toast.LENGTH_SHORT).show();
                            saveUserDetails();
                        }
                    });
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(CreateAccount.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
}






