package com.example.facebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;



public class MainPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private Button uploadImageButton;
    private Uri imageUri;
    private List<Post> posts;
    private Uri selectedImageUri;


    FirebaseFirestore db ;
    private static final int PICK_IMAGE_REQUEST = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mian_page);

        recyclerView = findViewById(R.id.recyclerView);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this);
        recyclerView.setAdapter(postAdapter);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        loadPosts();
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
                // Now you have the selected image URI, you can upload it to Firebase Storage
                uploadImage(selectedImageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        String documentId = UUID.randomUUID().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference imagesCollection = db.collection("images");

        // Set the image URL in the Firestore document
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", "");
    }


    private void loadPosts() {
        db.collection("posts")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Convert the query result to a list of Post objects
                        List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);

                        // Update the adapter with the loaded posts
                       setPosts(posts);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle database fetch failure
                    }
                });
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
        postAdapter.notifyDataSetChanged();
    }


}
