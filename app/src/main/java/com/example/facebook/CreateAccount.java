package com.example.facebook;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
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
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;

public class CreateAccount extends AppCompatActivity {

    public static final String TAG = "Tag";
    static final int PICK_IMAGE_REQUEST = 1;

    EditText FullName,Email,Password,Phone;
    Button RegisterBtn,selectedImageBtn;
    TextView LoginBtn;
    FirebaseAuth Auth;
    ProgressBar progressBar;
    FirebaseFirestore store;
    String userID;
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView profileImageView;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        FullName = findViewById(R.id.editTextname);
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        Phone = findViewById(R.id.editTextPhone);
        RegisterBtn = findViewById(R.id.Registerbtn);
        LoginBtn = findViewById(R.id.textCreateHere);
        progressBar = findViewById(R.id.progressBar);
        Auth = FirebaseAuth.getInstance();
        store =FirebaseFirestore.getInstance();
        profileImageView = findViewById(R.id.profileImageView);
        selectedImageBtn = findViewById(R.id.selectImageButton);



        if (Auth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();

        }


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String phone = Phone.getText().toString();
                String name = FullName.getText().toString();

                if (TextUtils.isEmpty(email)){
                    Email.setError("Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Password.setError("Password is Required");
                    return;
                }
                if (password.length()<6){
                    Password.setError("Password must be above 6 Characters");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                Auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(CreateAccount.this, "User Created", Toast.LENGTH_SHORT).show();
                            userID =Auth.getCurrentUser().getUid();
                            DocumentReference documentReference = store.collection("Users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("FullName",name);
                            user.put("email",email);
                            user.put("phone",phone);
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
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginAccount.class));

            }
        });

        selectedImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImageView.setImageURI(selectedImageUri);
            }
        }
    }

    
}






