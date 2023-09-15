package com.example.facebook;


import static android.content.ContentValues.TAG;
import static com.example.facebook.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginAccount extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView createBtn;
    ProgressBar progressBar2;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.login_account);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.loginbtn);
        createBtn = findViewById(R.id.textCreateHere);
        email = findViewById(R.id.editTextEmail);
        auth = FirebaseAuth.getInstance();
        progressBar2 = findViewById(R.id.progressBar2);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = LoginAccount.this.email.getText().toString().trim();
                String password = LoginAccount.this.password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    LoginAccount.this.email.setError("Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    LoginAccount.this.password.setError("Password is Required");
                    return;
                }
                if (password.length() < 6) {
                    LoginAccount.this.password.setError("Password must be above 6 Characters");
                    return;
                }
                progressBar2.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginAccount.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainPage.class));
                                    Log.e(TAG, "Navigation to MainPage activity");

                                } else {
                                    Log.e(TAG, "Authentication failed", task.getException());
                                    Toast.makeText(LoginAccount.this, "Please Enter valid mail or Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }

        });


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateAccount.class));

            }
        });

    }
}