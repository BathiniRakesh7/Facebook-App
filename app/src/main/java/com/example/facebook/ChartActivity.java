package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChartActivity extends AppCompatActivity {
    private Toolbar chartToolBar;
    private ImageButton sendMessageBtn, sendImageBtn;
    private EditText userMessageInput;
    private RecyclerView usersMessageList;
    private  final List<Messages>messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter ;
    private String messageReceiverId, messageReceiverName,messageSenderId,saveCurrentDate,saveCurrentTime;

    private TextView receiverName,userLastSeen;
    private CircleImageView receiverProfileImage;
    private FirebaseFirestore rootRef;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visitUserid").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        chartToolBar = findViewById(R.id.chart_bar_layout);
        setSupportActionBar(chartToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chart_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        receiverName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = findViewById(R.id.custom_profile_image);
        sendMessageBtn = findViewById(R.id.send_message_button);
        sendImageBtn = findViewById(R.id.send_image_button);
        userMessageInput = findViewById(R.id.input_message);
        usersMessageList = findViewById(R.id.users_messages_list);

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        usersMessageList.setHasFixedSize(true);
        usersMessageList.setLayoutManager(linearLayoutManager);
        usersMessageList.setAdapter(messagesAdapter);


        displayReceiverInfo();
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        fetchMessages();
    }

    private void fetchMessages() {
        CollectionReference messagesCollection = rootRef.collection("Messages")
                .document(messageSenderId)
                .collection(messageReceiverId);

        messagesCollection.orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        messagesList.clear();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Messages messages = documentSnapshot.toObject(Messages.class);
                            messagesList.add(messages);
                        }

                        Collections.reverse(messagesList);

                        messagesAdapter.notifyDataSetChanged();
                    }
                });
    }



    private void sendMessage() {
        updateUserStatus("online");
        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please type a Message", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;
            CollectionReference messagesCollection = rootRef.collection("Messages");
            DocumentReference newMessageDocRef = messagesCollection.document();

            // Generate a unique message ID
            String message_push_id = newMessageDocRef.getId();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            String saveCurrentDate = currentDate.format(calFordDate.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            String saveCurrentTime = currentTime.format(calFordDate.getTime());
            Map<String, Object> messageTextStructure = new HashMap<>();
            messageTextStructure.put("message", messageText);
            messageTextStructure.put("time", saveCurrentTime);
            messageTextStructure.put("date", saveCurrentDate);
            messageTextStructure.put("type", "text");
            messageTextStructure.put("messageFrom", messageSenderId);

            // Create a map to update multiple locations in Firestore
            Map<String, Object> messageStructureDetails = new HashMap<>();
            messageStructureDetails.put(messageSenderRef + "/" + message_push_id, messageTextStructure);
            messageStructureDetails.put(messageReceiverRef + "/" + message_push_id, messageTextStructure);
            rootRef.runTransaction(transaction -> {
                for (Map.Entry<String, Object> entry : messageStructureDetails.entrySet()) {
                    transaction.set(rootRef.document(entry.getKey()), entry.getValue());
                }
                return null;
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ChartActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                    userMessageInput.setText("");

                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(ChartActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    userMessageInput.setText("");

                }
            });
        }
    }
    public void updateUserStatus(String state){
        String saveCurrentDate ,saveCurrentTime;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        Map<String,Object> currentStateMap = new HashMap<>();
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("type",state);

        rootRef.collection("Users").document(messageSenderId).update("userState",currentStateMap);
    }

    private void displayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        rootRef.collection("Users").document(messageReceiverId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if(snapshot.exists()){
                    final String type = snapshot.getString("userState.type");
                    final String lastDate = snapshot.getString("userState.date");
                    final String lastTime = snapshot.getString("userState.time");
//                    final String  profileImage = snapshot.getString("profileImage");
//                    Picasso.with(ChartActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);

                    assert type != null;
                    if(type.equals("online")){
                        userLastSeen.setText("online");
                    }
                    else {
                        userLastSeen.setText("Last seen: " + lastTime+" " + lastDate);
                    }
                }

            }
        });
    }
}