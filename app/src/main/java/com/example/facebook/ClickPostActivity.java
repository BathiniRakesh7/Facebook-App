package com.example.facebook;

import static com.example.facebook.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView postImage;
    private TextView postDescription;
    private Button editPostButton, deletePostButton;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;
    private String postId, currentUserId, databaseUserid, description, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postId = getIntent().getExtras().get("postId").toString();
        firebaseFirestore = FirebaseFirestore.getInstance();

        postImage = findViewById(R.id.click_post_image);
        postDescription = findViewById(id.click_post_description);
        editPostButton = findViewById(id.click_edit_post_button);
        deletePostButton = findViewById(id.click_delete_post_button);

        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);


        firebaseFirestore.collection("Posts").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            description = documentSnapshot.getString("description");
                            image = documentSnapshot.getString("postImage");
                            databaseUserid = documentSnapshot.getString("uid");
                            postDescription.setText(description);
                            Picasso.get().load(image).into(postImage);
                            if (currentUserId.equals(databaseUserid)) {
                                deletePostButton.setVisibility(View.VISIBLE);
                                editPostButton.setVisibility(View.VISIBLE);
                            }
                            editPostButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    editCurrentPost(description);
                                }
                            });
                        } else {
                            Toast.makeText(ClickPostActivity.this, "Error in getting data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });

    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newDescription = inputField.getText().toString();
                firebaseFirestore.collection("Posts").document(postId).update("description", newDescription)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
                                postDescription.setText(newDescription);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ClickPostActivity.this, "Error updating description: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(color.lightBlue);

    }

    private void deleteCurrentPost() {
        firebaseFirestore.collection("Posts").document(postId).delete();
        sendUserMainActivity();
        Toast.makeText(this, "Post has been Deleted", Toast.LENGTH_SHORT).show();

    }

    private void sendUserMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }
}

