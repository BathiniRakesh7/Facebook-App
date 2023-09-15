package com.example.facebook;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    public static final String TAG = "Tag";
    static final int PICK_IMAGE_REQUEST = 1;

    EditText fullName, email, password, phone;
    Button registerBtn,selectedImageBtn;
    TextView loginBtn;
    FirebaseAuth auth;
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
        selectedImageBtn = findViewById(R.id.selectImageButton);



        if (auth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();

        }


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        loginBtn.setOnClickListener(new View.OnClickListener() {
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






