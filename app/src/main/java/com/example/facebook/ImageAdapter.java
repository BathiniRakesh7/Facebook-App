package com.example.facebook;

import android.content.Context;
;
import android.text.TextUtils;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mListener;

    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getName());
        holder.like_text.setText(String.valueOf(uploadCurrent.getLikes()));

        Picasso.get().load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);
        holder.loadComments(uploadCurrent.getKey(), holder.comment_container);



        if (holder.like_btn != null) {
            final boolean[] isLiked = {false};
            int initialLikeCount = uploadCurrent.getLikes();


            holder.like_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int updatedLikes = initialLikeCount;

                        if (!isLiked[0]) {
                            updatedLikes++;
                            holder.like_btn.setImageResource(R.drawable.baseline_favorite_24);
                        } else {
                            updatedLikes--;
                            holder.like_btn.setImageResource(R.drawable.baseline_favorite_border_24);
                            updatedLikes = initialLikeCount;
                        }

                        holder.like_text.setText(String.valueOf(updatedLikes));
                        isLiked[0] = !isLiked[0];

                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        String documentId = uploadCurrent.getKey();
                        DocumentReference documentRef = firestore.collection("uploads")
                                .document(documentId);

                        documentRef.update("likes", updatedLikes)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(mContext, isLiked[0] ? "You Liked the Image" : "You Disliked the Image", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(mContext, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


            });
        } else {
            Toast.makeText(mContext, "Error: Like button or Upload is null", Toast.LENGTH_SHORT).show();
        }

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        mListener.onCommentClick(adapterPosition);
                        String commentText = holder.comment_text.getText().toString().trim();
                        if (!TextUtils.isEmpty(commentText)) {
                            saveCommentToFirestore(uploadCurrent.getKey(), commentText);
                            holder.loadComments(uploadCurrent.getKey(), holder.comment_container);


                            holder.comment_text.setText("");
                        } else {
                            Toast.makeText(mContext, "Please enter a comment.", Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            }
        });
    }
    private void saveCommentToFirestore(String uploadKey, String commentText) {
        if (!TextUtils.isEmpty(commentText)) {
            // Save the comment to Firebase Firestore
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference uploadsRef = firestore.collection("uploads");

            // Get a reference to the specific document in the "uploads" collection
            DocumentReference documentRef = uploadsRef.document(uploadKey);

            // Update the comments field of the document by appending the new comment
            documentRef.update("comments", FieldValue.arrayUnion(commentText))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Comment saved successfully
                            Toast.makeText(mContext, "Comment posted.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error occurred while saving the comment
                            Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(mContext, "Please enter a comment.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView textViewName;
        public ImageView imageView;
        private final ImageView like_btn ;
        private final TextView like_text;
        public ImageView commentBtn;
        public EditText comment_text;
        public TextView enteredComment;
        public LinearLayout comment_container;

        public ImageViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
            like_text = itemView.findViewById(R.id.like_text);
            like_btn = itemView.findViewById(R.id.like_btn);
            commentBtn = itemView.findViewById(R.id.button_post_comment);
            comment_text = itemView.findViewById(R.id.edit_text_comment);
            enteredComment = itemView.findViewById(R.id.text_view_entered_comments);
            comment_container = itemView.findViewById(R.id.comment_item_layout);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem doWhatever = menu.add(Menu.NONE, 1, 1, "Do whatever");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "Delete");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    switch (item.getItemId()) {
                        case 1:
                            mListener.onWhatEverClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
        private void loadComments(String uploadKey, LinearLayout commentContainer) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference uploadsRef = firestore.collection("uploads");
            DocumentReference documentRef = uploadsRef.document(uploadKey);

            documentRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                List<String> comments = (List<String>) documentSnapshot.get("comments");
                                if (comments != null && !comments.isEmpty()) {
                                    commentContainer.removeAllViews();

                                    for (String commentText : comments) {
                                        LinearLayout commentLayout = new LinearLayout(mContext);
                                        commentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        commentLayout.setOrientation(LinearLayout.VERTICAL);

                                        TextView commentTextView = new TextView(mContext);
                                        commentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        commentTextView.setText(commentText);
                                        LinearLayout buttonsLayout = new LinearLayout(mContext);
                                        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

                                        ImageView likeButton = new ImageView(mContext);
                                        likeButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                                        TextView likeCountTextView = new TextView(mContext);
                                        likeCountTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        likeCountTextView.setText("0");


                                        AtomicBoolean isLiked = new AtomicBoolean(false);
                                        final int[] likeCount = {0};
                                        likeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!isLiked.get()) {
                                                    likeButton.setImageResource(R.drawable.baseline_favorite_24);
                                                    likeCount[0]++;
                                                    likeCountTextView.setText(likeCount[0] + (likeCount[0] == 1 ? " like" : " likes"));
                                                } else {
                                                    likeButton.setImageResource(R.drawable.baseline_favorite_border_24);

                                                    likeCount[0]--;
                                                    likeCountTextView.setText(likeCount[0] + (likeCount[0] == 1 ? " like" : " likes"));
                                                }
                                                isLiked.set(!isLiked.get());
                                            }
                                        });

                                        ImageView replyButton = new ImageView(mContext);
                                        replyButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                        replyButton.setImageResource(R.drawable.baseline_reply_24);

                                        buttonsLayout.addView(likeButton);
                                        buttonsLayout.addView(likeCountTextView);
                                        buttonsLayout.addView(replyButton);

                                        commentLayout.addView(commentTextView);
                                        commentLayout.addView(buttonsLayout); // Add the buttons layout

                                        commentContainer.addView(commentLayout);
                                    }
                                } else {
                                    enteredComment.setText("No comments yet.");
                                }
                            } else {
                                enteredComment.setText("Document does not exist.");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            enteredComment.setText("Error loading comments: " + e.getMessage());
                        }
                    });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onWhatEverClick(int position);

        void onDeleteClick(int position);

        void onCommentClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
