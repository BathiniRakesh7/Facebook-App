package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentBtn;
    private EditText commentInputText;
    private RecyclerView commentsList;
    private DocumentReference usersRef,postsRef;
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private  CollectionReference commentsRef;
    private  String postId,currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postId = getIntent().getExtras().get("postId").toString();
        usersRef = FirebaseFirestore.getInstance().collection("Users").document(currentUserId);
        postsRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);
         commentsRef = postsRef.collection("Comments");

        postCommentBtn = findViewById(R.id.post_comment_button);
        commentInputText = findViewById(R.id.comment_input);


        commentsList = findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);


        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            String FullName = documentSnapshot.getString("FullName");
                            validateComment(FullName);
                       commentInputText.setText("");
                        } else {
                            Toast.makeText(CommentsActivity.this, "Data not Retrieved", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CommentsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Comments> options = new FirestoreRecyclerOptions.Builder<Comments>()
                .setQuery(commentsRef, Comments.class)
                .build();

        FirestoreRecyclerAdapter<Comments, CommentsActivity.CommentsViewHolder> adapter =
                new FirestoreRecyclerAdapter<Comments, CommentsActivity.CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsActivity.CommentsViewHolder holder, int position, @NonNull Comments model) {

                        holder.setComment(model.getComment());
                        holder.setUserName(model.getUserName());
                        holder.setDateTime(model.getDateTime());
                    }

                    @NonNull
                    @Override
                    public CommentsActivity.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_comments_layout, parent, false);
                        return new CommentsActivity.CommentsViewHolder(view);
                    }
                };
        commentsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder( View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView currentUserName = mView.findViewById(R.id.comment_user_name);
            currentUserName.setText(userName +" ");

        }

        public void setComment(String comment) {
            TextView currentUserComment = mView.findViewById(R.id.display_comment_text);
            currentUserComment.setText(comment);
        }

        public void setDateTime(String dateTime) {
            TextView commentDateTime = mView.findViewById(R.id.comment_date_time);
            commentDateTime.setText(" "+dateTime);
        }
    }

    private void validateComment(String FullName) {

        String commentText = commentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "please enter comment", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            final  String saveCurrentDateTime = currentDate.format(calFordDate.getTime());

            final String randomKey = currentUserId + saveCurrentDateTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",currentUserId);
            commentsMap.put("comment",commentText);
            commentsMap.put("dateTime",saveCurrentDateTime);
            commentsMap.put("userName",FullName);
            commentsRef.document(randomKey).set(commentsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CommentsActivity.this, "You have commented successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CommentsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("error", e.getMessage());
                        }
                    });
        }
    }
}