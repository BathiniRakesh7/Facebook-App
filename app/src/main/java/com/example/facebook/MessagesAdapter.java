package com.example.facebook;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private Task<DocumentSnapshot> usersRef;


    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_users, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getMessageFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseFirestore.getInstance().collection("Users").document(fromUserId).get();
        usersRef.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    ////                    final String  profileImage = snapshot.getString("profileImage");
////                    Picasso.with(ChartActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }
        });
        if (fromMessageType.equals("text")) {
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        }
        if (fromUserId.equals(messageSenderId)) {
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
            holder.senderMessageText.setTextColor(Color.BLACK);
            holder.senderMessageText.setGravity(Gravity.LEFT);
            holder.senderMessageText.setText(messages.getMessage());
        } else {
            holder.senderMessageText.setVisibility(View.INVISIBLE);

            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);

            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
            holder.receiverMessageText.setTextColor(Color.WHITE);
            holder.receiverMessageText.setGravity(Gravity.LEFT);
            holder.receiverMessageText.setText(messages.getMessage());


        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
